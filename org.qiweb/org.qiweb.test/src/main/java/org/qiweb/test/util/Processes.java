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
package org.qiweb.test.util;

import io.werval.util.Couple;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;

import static io.werval.util.IllegalArguments.ensureNotEmpty;

/**
 * Processes utilities.
 */
public final class Processes
{
    /**
     * Current Process PID.
     *
     * @param fallback Fallback in case detection fail
     *
     * @return Current Process PID or {@literal fallback} if unable to detect
     */
    public static String currentPID( String fallback )
    {
        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf( '@' );
        if( index < 1 )
        {
            // part before '@' empty (index = 0) / '@' not found (index = -1)
            return fallback;
        }
        try
        {
            return Long.toString( Long.parseLong( jvmName.substring( 0, index ) ) );
        }
        catch( NumberFormatException e )
        {
            // ignore
        }
        return fallback;
    }

    /**
     * List locally running JVM processes.
     *
     * @return List of Couple, with PID as left and textual info as right.
     */
    public static List<Couple<String, String>> jvmList()
    {
        return jvmList( null );
    }

    /**
     * List locally running JVM processes.
     *
     * @param filter Filter the JVM processes
     *
     * @return List of Couple, with PID as left and textual info as right.
     */
    public static List<Couple<String, String>> jvmList( Predicate<String> filter )
    {
        List<Couple<String, String>> list = new ArrayList<>();
        try
        {
            Process jps = new ProcessBuilder( "jps", "-mlv" ).start();
            Scanner scanner = new Scanner( jps.getInputStream() );
            while( scanner.hasNext() )
            {
                String line = scanner.nextLine();
                if( filter == null || filter.test( line ) )
                {
                    list.add(
                        Couple.of(
                            line.substring( 0, line.indexOf( ' ' ) ),
                            line.substring( line.indexOf( ' ' ) + 1 )
                        )
                    );
                }
            }
            if( jps.waitFor() != 0 )
            {
                throw new RuntimeException( "Unable to list jvm processes" );
            }
            return list;
        }
        catch( InterruptedException ex )
        {
            Thread.currentThread().interrupt();
            throw new RuntimeException( "Unable to list jvm processes", ex );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }

    /**
     * Kill all JVM Processes that match a given filter.
     *
     * @param filter Filter JVM Processes
     */
    public static void killJvms( Predicate<String> filter )
    {
        jvmList( filter ).stream().forEach( z -> kill( z.left() ) );
    }

    /**
     * Kill a Process.
     *
     * Use {@literal kill -9} on Unices, {@literal TASKKILL /PID} on Windows.
     *
     * @param pid PID of the Process to kill
     */
    public static void kill( String pid )
    {
        ensureNotEmpty( "pid", pid );
        ProcessBuilder kill;
        if( System.getProperty( "os.name" ).toLowerCase().contains( "win" ) )
        {
            kill = new ProcessBuilder( "TASKKILL", "/PID", pid );
        }
        else
        {
            kill = new ProcessBuilder( "kill", "-9", pid );
        }
        try
        {
            if( kill.start().waitFor() != 0 )
            {
                throw new RuntimeException( "Unable to kill process: " + pid );
            }
        }
        catch( InterruptedException ex )
        {
            Thread.currentThread().interrupt();
            throw new RuntimeException( "Unable to kill process: " + pid, ex );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }

    private Processes()
    {
    }
}
