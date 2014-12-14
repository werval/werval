/*
 * Copyright (c) 2011-2014 the original author or authors
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
package io.werval.modules.jpa.internal;

import io.werval.util.Strings;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.persistence.logging.AbstractSessionLog;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.logging.SessionLogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SLF4J Logging for EclipseLink.
 *
 * Froked from Miguel Angel Sosvilla Luis <a href="https://gist.github.com/msosvi/1325764">gist</a>.
 * <p>
 * In order to register this class with EclipseLink, set the value of property
 * <code>eclipselink.logging.logger</code> to this class' fully qualified name
 * (<code>org.eclipse.persistence.logging.Slf4jSessionLogger</code>).
 * <p>
 * Once registered, log level configuration is no longer defined within EclipseLink (using property
 * <code>eclipselink.logging.level</code>), but within whatever SL4J implementation is being used
 * (<code>logback.xml</code> for <code>logback</code>, <code>log4j.properties</code> for <code>log4j</code>, etc.).
 * <p>
 * The rest of EclipseLink logging properties can still be used (<code>eclipselink.logging.timestamp</code>,
 * <code>eclipselink.logging.thread</code>, <code>eclipselink.logging.session</code>,
 * <code>eclipselink.logging.connection</code> and <code>eclipselink.logging.parameters</code>) to configure the output
 * format.
 * <p>
 * The following log categories are available:
 * <ul>
 * <li>org.eclipse.persistence.logging.default</li>
 * <li>org.eclipse.persistence.logging.sql</li>
 * <li>org.eclipse.persistence.logging.metadata</li>
 * <li>org.eclipse.persistence.logging.transaction</li>
 * <li>org.eclipse.persistence.logging.event</li>
 * <li>org.eclipse.persistence.logging.connection</li>
 * <li>org.eclipse.persistence.logging.query</li>
 * <li>org.eclipse.persistence.logging.cache</li>
 * <li>org.eclipse.persistence.logging.propagation</li>
 * <li>org.eclipse.persistence.logging.sequencing</li>
 * <li>org.eclipse.persistence.logging.ejb</li>
 * <li>org.eclipse.persistence.logging.ejb_or_metadata</li>
 * <li>org.eclipse.persistence.logging.weaver</li>
 * <li>org.eclipse.persistence.logging.properties</li>
 * <li>org.eclipse.persistence.logging.server</li>
 * </ul>
 * <p>
 * The mapping between EclipseLink and SLF4J log levels is as follows:
 * <ul>
 * <li>ALL,FINER,FINEST -&gt; TRACE</li>
 * <li>FINE -&gt; DEBUG</li>
 * <li>CONFIG,INFO -&gt; INFO</li>
 * <li>WARNING -&gt; WARN</li>
 * <li>SEVERE -&gt; ERROR</li>
 * </ul>
 */
public class Slf4jSessionLogger
    extends AbstractSessionLog
{
    public static final String ECLIPSELINK_NAMESPACE = "org.eclipse.persistence.logging";
    public static final String DEFAULT_CATEGORY = "default";
    public static final String DEFAULT_ECLIPSELINK_NAMESPACE = ECLIPSELINK_NAMESPACE + "." + DEFAULT_CATEGORY;

    private final Map<String, Logger> categoryLoggers;
    private final Map<Integer, LogLevel> mapLevels;

    public Slf4jSessionLogger()
    {
        super();
        // Initialize loggers eagerly
        categoryLoggers = new HashMap<>();
        for( String category : SessionLog.loggerCatagories )
        {
            categoryLoggers.put( category, LoggerFactory.getLogger( ECLIPSELINK_NAMESPACE + "." + category ) );
        }
        categoryLoggers.put( DEFAULT_CATEGORY, LoggerFactory.getLogger( DEFAULT_ECLIPSELINK_NAMESPACE ) );
        // Mapping between EclipseLink and SLF4J log levels.
        mapLevels = new HashMap<>();
        mapLevels.put( SessionLog.ALL, LogLevel.TRACE );
        mapLevels.put( SessionLog.FINEST, LogLevel.TRACE );
        mapLevels.put( SessionLog.FINER, LogLevel.TRACE );
        mapLevels.put( SessionLog.FINE, LogLevel.DEBUG );
        mapLevels.put( SessionLog.CONFIG, LogLevel.INFO );
        mapLevels.put( SessionLog.INFO, LogLevel.INFO );
        mapLevels.put( SessionLog.WARNING, LogLevel.WARN );
        mapLevels.put( SessionLog.SEVERE, LogLevel.ERROR );
    }

    @Override
    public void log( SessionLogEntry entry )
    {
        if( !shouldLog( entry.getLevel(), entry.getNameSpace() ) )
        {
            return;
        }
        Logger logger = getLogger( entry.getNameSpace() );
        LogLevel logLevel = getLogLevel( entry.getLevel() );
        StringBuilder message = new StringBuilder();
        message.append( getSupplementDetailString( entry ) );
        message.append( formatMessage( entry ) );
        switch( logLevel )
        {
            case TRACE:
                logger.trace( message.toString() );
                break;
            case DEBUG:
                logger.debug( message.toString() );
                break;
            case INFO:
                logger.info( message.toString() );
                break;
            case WARN:
                logger.warn( message.toString() );
                break;
            case ERROR:
                logger.error( message.toString() );
                break;
            default:
                throw new InternalError();
        }
    }

    @Override
    public boolean shouldLog( int level, String category )
    {
        Logger logger = getLogger( category );
        boolean resp = false;
        LogLevel logLevel = getLogLevel( level );
        switch( logLevel )
        {
            case TRACE:
                resp = logger.isTraceEnabled();
                break;
            case DEBUG:
                resp = logger.isDebugEnabled();
                break;
            case INFO:
                resp = logger.isInfoEnabled();
                break;
            case WARN:
                resp = logger.isWarnEnabled();
                break;
            case ERROR:
                resp = logger.isErrorEnabled();
                break;
            default:
                resp = true;
        }
        return resp;
    }

    @Override
    public boolean shouldLog( int level )
    {
        return shouldLog( level, "default" );
    }

    /**
     * @return Return true if SQL logging should log visible bind parameters. If the
     *         shouldDisplayData is not set, return false.
     */
    @Override
    public boolean shouldDisplayData()
    {
        if( this.shouldDisplayData != null )
        {
            return shouldDisplayData;
        }
        else
        {
            return false;
        }
    }

    /**
     * INTERNAL: Return the Logger for the given category
     */
    private Logger getLogger( String category )
    {
        if( Strings.isEmpty( category ) || !this.categoryLoggers.containsKey( category ) )
        {
            category = DEFAULT_CATEGORY;
        }
        return categoryLoggers.get( category );
    }

    /**
     * Return the corresponding Slf4j Level for a given EclipseLink level.
     */
    private LogLevel getLogLevel( Integer level )
    {
        LogLevel logLevel = mapLevels.get( level );

        if( logLevel == null )
        {
            logLevel = LogLevel.OFF;
        }
        return logLevel;
    }

    /**
     * SLF4J log levels.
     */
    enum LogLevel
    {
        TRACE, DEBUG, INFO, WARN, ERROR, OFF
    }
}
