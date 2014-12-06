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
package org.qiweb.modules.rythm;

import org.rythmengine.extension.ILoggerFactory;
import org.rythmengine.logger.ILogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rythm ILoggerFactory to make Rythm use SLF4J.
 *
 * Automatically registered by {@link RythmPlugin}.
 */
/* package */ final class SLF4JLoggerFactory
    implements ILoggerFactory
{
    @Override
    public ILogger getLogger( Class<?> clazz )
    {
        return new SLF4JLogger( clazz );
    }

    private static final class SLF4JLogger
        implements ILogger
    {
        private final Logger logger;

        private SLF4JLogger( Class<?> clazz )
        {
            logger = LoggerFactory.getLogger( clazz );
        }

        @Override
        public boolean isTraceEnabled()
        {
            return logger.isTraceEnabled();
        }

        @Override
        public void trace( String format, Object... args )
        {
            logger.trace( format, args );
        }

        @Override
        public void trace( Throwable t, String format, Object... args )
        {
            logger.trace( format, args, t );
        }

        @Override
        public boolean isDebugEnabled()
        {
            return logger.isDebugEnabled();
        }

        @Override
        public void debug( String format, Object... args )
        {
            logger.debug( format, args );
        }

        @Override
        public void debug( Throwable t, String format, Object... args )
        {
            logger.debug( format, args, t );
        }

        @Override
        public boolean isInfoEnabled()
        {
            return logger.isInfoEnabled();
        }

        @Override
        public void info( String format, Object... arg )
        {
            logger.info( format, arg );
        }

        @Override
        public void info( Throwable t, String format, Object... args )
        {
            logger.info( format, args, t );
        }

        @Override
        public boolean isWarnEnabled()
        {
            return logger.isWarnEnabled();
        }

        @Override
        public void warn( String format, Object... arg )
        {
            logger.warn( format, arg );
        }

        @Override
        public void warn( Throwable t, String format, Object... args )
        {
            logger.warn( format, args, t );
        }

        @Override
        public boolean isErrorEnabled()
        {
            return logger.isErrorEnabled();
        }

        @Override
        public void error( String format, Object... arg )
        {
            logger.error( format, arg );
        }

        @Override
        public void error( Throwable t, String format, Object... args )
        {
            logger.error( format, args, t );
        }
    }
}
