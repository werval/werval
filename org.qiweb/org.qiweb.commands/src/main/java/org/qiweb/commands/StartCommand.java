/*
 * Copyright (c) 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qiweb.commands;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.qiweb.api.exceptions.QiWebException;

import static org.qiweb.api.exceptions.IllegalArguments.ensureNotEmpty;
import static org.qiweb.api.exceptions.IllegalArguments.ensureNotNull;

/**
 * Start Command.
 */
public class StartCommand
    implements Runnable
{
    public static enum ExecutionModel
    {
        /**
         * Run in isolated threads, good for build plugins.
         */
        ISOLATED_THREADS,
        /**
         * Fork a java process, good for CLI.
         */
        FORK
    }

    private static final long DAEMON_THREAD_JOIN_TIMEOUT = 15000;
    private static final boolean STOP_UNRESPONSIVE_DAEMON_THREADS = false;
    private static final boolean CLEANUP_DAEMON_THREADS = true;

    private final ExecutionModel executionModel;
    private final String mainClass;
    private final String[] arguments;
    private final URL[] classpath;

    public StartCommand( ExecutionModel executionModel, String mainClass, String[] arguments, URL[] classpath )
    {
        ensureNotNull( "Execution Model", executionModel );
        ensureNotEmpty( "Main class", mainClass );
        this.executionModel = executionModel;
        this.mainClass = mainClass;
        this.arguments = arguments == null ? new String[ 0 ] : arguments;
        this.classpath = classpath == null ? new URL[ 0 ] : classpath;
    }

    @Override
    public void run()
    {
        switch( executionModel )
        {
            case ISOLATED_THREADS:
                runIsolatedThreads();
                break;
            case FORK:
                runFork();
                break;
            default:
                throw new InternalError();
        }
    }

    private void runFork()
    {
        // java -cp class:path mainClass arg um ents
        List<String> cmd = new ArrayList<>();
        cmd.add( "java" );
        cmd.add( "-cp" );
        StringBuilder cpBuilder = new StringBuilder();
        Iterator<URL> cpIt = Arrays.asList( classpath ).iterator();
        while( cpIt.hasNext() )
        {
            cpBuilder.append( cpIt.next().toString() );
            if( cpIt.hasNext() )
            {
                cpBuilder.append( ":" );
            }
        }
        cmd.add( cpBuilder.toString() );
        cmd.add( mainClass );
        cmd.addAll( Arrays.asList( arguments ) );
        try
        {
            Process process = new ProcessBuilder( cmd ).start();
            int status = process.waitFor();
            if( status != 0 )
            {
                throw new QiWebException(
                    "An exception occured while executing the QiWeb Application, status was: " + status
                );
            }
        }
        catch( IOException ex )
        {
            throw new QiWebException( "An exception occured while executing the QiWeb Application.", ex );
        }
        catch( InterruptedException ex )
        {
            Thread.interrupted();
            throw new QiWebException( "An exception occured while executing the QiWeb Application.", ex );
        }
    }

    private void runIsolatedThreads()
    {
        IsolatedThreadGroup threadGroup = new IsolatedThreadGroup( mainClass /* name */ );
        Thread bootstrapThread = new Thread(
            threadGroup,
            () ->
            {
                try
                {
                    invokeMain();
                }
                catch( NoSuchMethodException ex )
                {
                    Thread.currentThread().getThreadGroup().uncaughtException(
                        Thread.currentThread(),
                        new Exception(
                            "The specified mainClass doesn't contain a main method with appropriate signature.",
                            ex
                        )
                    );
                }
                catch( InvocationTargetException ex )
                {
                    Thread.currentThread().getThreadGroup().uncaughtException(
                        Thread.currentThread(),
                        ex.getCause()
                    );
                }
                catch( Exception ex )
                {
                    Thread.currentThread().getThreadGroup().uncaughtException(
                        Thread.currentThread(),
                        ex
                    );
                }
            },
            mainClass + ".main()"
        );

        bootstrapThread.setContextClassLoader( new URLClassLoader( classpath ) );
        bootstrapThread.start();
        joinNonDaemonThreads( threadGroup );

        if( CLEANUP_DAEMON_THREADS )
        {
            terminateThreads( threadGroup );
            try
            {
                threadGroup.destroy();
            }
            catch( IllegalThreadStateException ex )
            {
                System.err.println( "Couldn't destroy threadgroup " + threadGroup );
                ex.printStackTrace( System.err );
            }
        }

        synchronized( threadGroup )
        {
            if( threadGroup.uncaughtException != null )
            {
                throw new QiWebException(
                    "An exception occured while executing the QiWeb Application. "
                    + threadGroup.uncaughtException.getMessage(),
                    threadGroup.uncaughtException
                );
            }
        }
    }

    private void invokeMain()
        throws ClassNotFoundException, NoSuchMethodException,
               IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Method main = Thread.currentThread().getContextClassLoader().loadClass(
            mainClass
        ).getMethod(
            "main",
            new Class[]
            {
                String[].class
            }
        );
        if( !main.isAccessible() )
        {
            main.setAccessible( true );
        }
        if( !Modifier.isStatic( main.getModifiers() ) )
        {
            throw new IllegalArgumentException(
                "Can't call main(String[]) method because it is not static."
            );
        }
        main.invoke(
            null,
            new Object[]
            {
                arguments
            }
        );
    }

    /**
     * a ThreadGroup to isolate execution and collect exceptions.
     */
    class IsolatedThreadGroup
        extends ThreadGroup
    {
        private Throwable uncaughtException; // synchronize access to this

        public IsolatedThreadGroup( String name )
        {
            super( name );
        }

        @Override
        public void uncaughtException( Thread thread, Throwable throwable )
        {
            if( throwable instanceof ThreadDeath )
            {
                return; // harmless
            }
            synchronized( this )
            {
                if( uncaughtException == null ) // only remember the first one
                {
                    uncaughtException = throwable; // will be reported eventually
                }
            }
            throwable.printStackTrace( System.err );
        }
    }

    private void joinNonDaemonThreads( ThreadGroup threadGroup )
    {
        boolean foundNonDaemon;
        do
        {
            foundNonDaemon = false;
            Collection<Thread> threads = getActiveThreads( threadGroup );
            for( Thread thread : threads )
            {
                if( thread.isDaemon() )
                {
                    continue;
                }
                foundNonDaemon = true; // try again; maybe more threads were created while we were busy
                joinThread( thread, 0 );
            }
        }
        while( foundNonDaemon );
    }

    private void joinThread( Thread thread, long timeoutMsecs )
    {
        try
        {
            // System.out.println(  "joining on thread " + thread );
            thread.join( timeoutMsecs );
        }
        catch( InterruptedException e )
        {
            Thread.currentThread().interrupt(); // good practice if don't throw
            System.err.println( "interrupted while joining against thread " + thread ); // not expected!
            e.printStackTrace( System.err );
        }
        if( thread.isAlive() ) // generally abnormal
        {
            System.err.println(
                "thread " + thread + " was interrupted but is still alive after at least " + timeoutMsecs + "msecs"
            );
        }
    }

    private void terminateThreads( ThreadGroup threadGroup )
    {
        long startTime = System.currentTimeMillis();
        Set<Thread> uncooperativeThreads = new HashSet<>(); // these were not responsive to interruption
        for( Collection<Thread> threads = getActiveThreads( threadGroup );
             !threads.isEmpty();
             threads = getActiveThreads( threadGroup ), threads.removeAll( uncooperativeThreads ) )
        {
            // Interrupt all threads we know about as of this instant (harmless if spuriously went dead (! isAlive())
            // or if something else interrupted it ( isInterrupted() ).
            for( Thread thread : threads )
            {
                // System.out.println(  "interrupting thread " + thread );
                thread.interrupt();
            }
            // Now join with a timeout and call stop() (assuming flags are set right)
            for( Thread thread : threads )
            {
                if( !thread.isAlive() )
                {
                    continue; // and, presumably it won't show up in getActiveThreads() next iteration
                }
                if( DAEMON_THREAD_JOIN_TIMEOUT <= 0 )
                {
                    joinThread( thread, 0 ); // waits until not alive; no timeout
                    continue;
                }
                long timeout = DAEMON_THREAD_JOIN_TIMEOUT - ( System.currentTimeMillis() - startTime );
                if( timeout > 0 )
                {
                    joinThread( thread, timeout );
                }
                if( !thread.isAlive() )
                {
                    continue;
                }
                uncooperativeThreads.add( thread ); // ensure we don't process again
                if( STOP_UNRESPONSIVE_DAEMON_THREADS )
                {
                    System.err.println( "thread " + thread + " will be Thread.stop()'ed" );
                    thread.stop();
                }
                else
                {
                    System.err.println(
                        "thread " + thread + " will linger despite being asked to die via interruption"
                    );
                }
            }
        }
        if( !uncooperativeThreads.isEmpty() )
        {
            System.err.println( "NOTE: " + uncooperativeThreads.size()
                                + " thread(s) did not finish despite being asked to via interruption."
                                + " This is not a problem with QiWeb Run, it is a problem with the running code."
                                + " Although not serious, it should be remedied." );
        }
        else
        {
            int activeCount = threadGroup.activeCount();
            if( activeCount != 0 )
            {
                // TODO this may be nothing; continue on anyway; perhaps don't even log in future
                Thread[] threadsArray = new Thread[ 1 ];
                threadGroup.enumerate( threadsArray );

                System.err.println(
                    "strange; " + activeCount + " thread(s) still active in the group " + threadGroup
                    + " such as " + threadsArray[0]
                );
            }
        }
    }

    private Collection<Thread> getActiveThreads( ThreadGroup threadGroup )
    {
        Thread[] threads = new Thread[ threadGroup.activeCount() ];
        int numThreads = threadGroup.enumerate( threads );
        Collection<Thread> result = new ArrayList<>( numThreads );
        for( int i = 0; i < threads.length && threads[i] != null; i++ )
        {
            result.add( threads[i] );
        }
        return result; // note: result should be modifiable
    }
}
