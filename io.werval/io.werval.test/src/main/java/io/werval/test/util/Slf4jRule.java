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
package io.werval.test.util;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.read.ListAppender;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;

/**
 * SLF4J JUnit Rule for use with Logback.
 */
// Inspired by https://gist.github.com/tux2323/1005996
public class Slf4jRule
    extends TestWatcher
{
    /**
     * Logging Level.
     *
     * @hidden
     */
    public enum Level
    {
        TRACE( ch.qos.logback.classic.Level.TRACE ),
        DEBUG( ch.qos.logback.classic.Level.DEBUG ),
        INFO( ch.qos.logback.classic.Level.INFO ),
        WARN( ch.qos.logback.classic.Level.WARN ),
        ERROR( ch.qos.logback.classic.Level.ERROR );

        private final ch.qos.logback.classic.Level internalLevel;

        private Level( ch.qos.logback.classic.Level level )
        {
            this.internalLevel = level;
        }
    }

    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    private final List<String> sources = new CopyOnWriteArrayList<>();
    private Level level = Level.TRACE;

    @Override
    protected void starting( Description description )
    {
        resetContext();
        sources.stream().forEach( source -> addAppenderToName( source ) );
        appender.start();
    }

    @Override
    protected void finished( Description description )
    {
        appender.stop();
        resetContext();
    }

    public void record( Level level )
    {
        this.level = level;
    }

    public void recordForObject( Object obj )
    {
        recordForType( obj.getClass() );
    }

    public void recordForType( Class<?> type )
    {
        recordForName( type.getName() );
    }

    public void recordForPackage( Package pack )
    {
        recordForName( pack.getName() );
    }

    public void recordForName( String name )
    {
        sources.add( name );
        addAppenderToName( name );
    }

    public List<String> allStatements()
    {
        return appender.list.stream().map( s -> s.getFormattedMessage() ).collect( toList() );
    }

    public boolean contains( String text )
    {
        return appender.list.stream().anyMatch( event -> event.getFormattedMessage().contains( text ) );
    }

    public boolean containsMatching( String regex )
    {
        return appender.list.stream().anyMatch( event -> event.getFormattedMessage().matches( regex ) );
    }

    public boolean containsExMessage( String exceptionMessage )
    {
        return appender.list.stream().anyMatch(
            event ->
            {
                IThrowableProxy ex = event.getThrowableProxy();
                if( ex == null )
                {
                    return false;
                }
                if( ex.getMessage().contains( exceptionMessage ) )
                {
                    return true;
                }
                Stack<IThrowableProxy> stack = new Stack<>();
                stack.add( ex.getCause() );
                stack.addAll( Arrays.asList( ex.getSuppressed() ) );
                while( !stack.empty() )
                {
                    IThrowableProxy candidate = stack.pop();
                    if( candidate.getMessage().contains( exceptionMessage ) )
                    {
                        return true;
                    }
                    stack.add( candidate.getCause() );
                    stack.addAll( Arrays.asList( candidate.getSuppressed() ) );
                }
                return false;
            }
        );
    }

    public int size()
    {
        return appender.list.size();
    }

    public void clear()
    {
        appender.list.clear();
    }

    private <T> void addAppenderToName( String name )
    {
        Logger logger = (Logger) LoggerFactory.getLogger( name );
        logger.addAppender( appender );
        logger.setLevel( level.internalLevel );
    }

    private void resetContext()
    {
        context.reset();
    }
}
