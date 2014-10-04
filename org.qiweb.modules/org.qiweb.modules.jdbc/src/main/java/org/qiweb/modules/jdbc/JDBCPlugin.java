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
package org.qiweb.modules.jdbc;

import com.jolbox.bonecp.BoneCPDataSource;
import com.jolbox.bonecp.ConnectionHandle;
import com.jolbox.bonecp.hooks.AbstractConnectionHook;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.qiweb.api.Application;
import org.qiweb.api.Config;
import org.qiweb.api.Plugin;
import org.qiweb.api.exceptions.ActivationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qiweb.util.Strings.EMPTY;
import static org.qiweb.util.Strings.isEmpty;

/**
 * JDBC Plugin that manage DataSources using BoneCP pool.
 */
public class JDBCPlugin
    implements Plugin<JDBC>
{
    /* package */ static final String DEFAULT_DATASOURCE = "jdbc.default_datasource";
    private static final String DATASOURCES = "jdbc.datasources";
    private static final Logger BONECP_LOG = LoggerFactory.getLogger( "com.jolbox.bonecp" );
    private JDBC jdbc;

    @Override
    public void onActivate( Application application )
        throws ActivationException
    {
        Config config = application.config();
        Map<String, BoneCPDataSource> dataSources = new HashMap<>();
        if( config.has( DATASOURCES ) )
        {
            Config allDsConfig = config.object( DATASOURCES );
            for( String dsName : allDsConfig.subKeys() )
            {
                Config dsConfig = allDsConfig.object( dsName );
                BoneCPDataSource ds = createDataSource( dsName, dsConfig, application.classLoader() );
                dataSources.put( dsName, ds );
            }
        }
        jdbc = new JDBC( dataSources, config.has( DEFAULT_DATASOURCE ) ? config.string( DEFAULT_DATASOURCE ) : null );
    }

    @Override
    public void onPassivate( Application application )
    {
        if( jdbc != null )
        {
            jdbc.passivate();
            jdbc = null;
        }
    }

    @Override
    public Class<JDBC> apiType()
    {
        return JDBC.class;
    }

    @Override
    public JDBC api()
    {
        return jdbc;
    }

    private BoneCPDataSource createDataSource( String dsName, Config dsConfig, ClassLoader loader )
    {
        // JDBC configuration
        String driver = dsConfig.string( "driver" );
        String url = dsConfig.string( "url" );
        String user = null;
        String password = null;
        try
        {
            // Extract username/password from the URL if available
            // This provide Heroku DATABASE_URL syntax support
            URI uri = new URI( url );
            if( !isEmpty( uri.getUserInfo() ) )
            {
                String[] userInfo = uri.getUserInfo().split( ":" );
                if( userInfo.length > 0 )
                {
                    user = userInfo[0];
                    if( userInfo.length > 1 )
                    {
                        password = userInfo[1];
                    }
                    // Resolve Scheme if needed
                    String scheme;
                    switch( uri.getScheme() )
                    {
                        case "postgres":
                            scheme = "postgresql";
                            break;
                        default:
                            scheme = uri.getScheme();
                            break;
                    }
                    // Remove UserInfo
                    url = scheme
                          + "://"
                          + ( uri.getHost() != null ? uri.getHost() : EMPTY )
                          + ( uri.getPort() != -1 ? ":" + uri.getPort() : EMPTY )
                          + ( uri.getPath() != null ? uri.getPath() : EMPTY )
                          + ( uri.getQuery() != null ? uri.getQuery() : EMPTY )
                          + ( uri.getFragment() != null ? uri.getFragment() : EMPTY );
                }
            }
        }
        catch( URISyntaxException ex )
        {
            throw new ActivationException( "Invalid JDBC URI: " + url, ex );
        }
        if( !url.startsWith( "jdbc:" ) )
        {
            url = "jdbc:" + url;
        }
        if( dsConfig.has( "user" ) )
        {
            user = dsConfig.string( "user" );
        }
        if( dsConfig.has( "password" ) )
        {
            password = dsConfig.string( "password" );
        }

        // Connection configuration
        final boolean autocommit = dsConfig.has( "autocommit" ) ? dsConfig.bool( "autocommit" ) : true;
        final Integer isolation;
        if( dsConfig.has( "isolation" ) )
        {
            String isolationString = dsConfig.string( "isolation" );
            switch( isolationString )
            {
                case "NONE":
                    isolation = Connection.TRANSACTION_NONE;
                    break;
                case "READ_COMMITED":
                    isolation = Connection.TRANSACTION_READ_COMMITTED;
                    break;
                case "READ_UNCOMMITED":
                    isolation = Connection.TRANSACTION_READ_UNCOMMITTED;
                    break;
                case "REPEATABLE_READ":
                    isolation = Connection.TRANSACTION_REPEATABLE_READ;
                    break;
                case "SERIALIZABLE":
                    isolation = Connection.TRANSACTION_SERIALIZABLE;
                    break;
                default:
                    throw new IllegalArgumentException(
                        "Unknown isolation level '" + isolationString + "' for DataSource '" + dsName + "'"
                    );
            }
        }
        else
        {
            isolation = null;
        }
        final String defaultCatalog = dsConfig.has( "defaultCatalog" ) ? dsConfig.string( "defaultCatalog" ) : null;
        final boolean readOnly = dsConfig.has( "readOnly" ) ? dsConfig.bool( "readOnly" ) : false;

        // Setup DataSource
        try
        {
            DriverManager.registerDriver( (Driver) Class.forName( driver, true, loader ).newInstance() );
            BoneCPDataSource ds = new BoneCPDataSource();
            ds.setClassLoader( loader );
            ds.setConnectionHook(
                new AbstractConnectionHook()
                {
                    @Override
                    public void onCheckOut( ConnectionHandle connection )
                    {
                        try
                        {
                            connection.setAutoCommit( autocommit );
                            if( isolation != null )
                            {
                                connection.setTransactionIsolation( isolation );
                            }
                            connection.setReadOnly( readOnly );
                            if( defaultCatalog != null )
                            {
                                connection.setCatalog( defaultCatalog );
                            }
                        }
                        catch( SQLException ex )
                        {
                            throw new RuntimeSQLException( ex );
                        }
                        if( BONECP_LOG.isTraceEnabled() )
                        {
                            BONECP_LOG.trace( "Check out connection {} [{} leased]", connection, ds.getTotalLeased() );
                        }
                    }

                    @Override
                    public void onCheckIn( ConnectionHandle connection )
                    {
                        if( BONECP_LOG.isTraceEnabled() )
                        {
                            BONECP_LOG.trace( "Check in connection {} [{} leased]", connection, ds.getTotalLeased() );
                        }
                    }
                }
            );
            ds.setJdbcUrl( url );
            ds.setUsername( user );
            ds.setPassword( password );

            // Pool configuration
            ds.setPartitionCount( dsConfig.has( "partitionCount" ) ? dsConfig.intNumber( "partitionCount" ) : 1 );
            ds.setMaxConnectionsPerPartition( dsConfig.has( "maxConnectionsPerPartition" ) ? dsConfig.intNumber( "maxConnectionsPerPartition" ) : 30 );
            ds.setMinConnectionsPerPartition( dsConfig.has( "minConnectionsPerPartition" ) ? dsConfig.intNumber( "minConnectionsPerPartition" ) : 5 );
            ds.setAcquireIncrement( dsConfig.has( "acquireIncrement" ) ? dsConfig.intNumber( "acquireIncrement" ) : 1 );
            ds.setAcquireRetryAttempts( dsConfig.has( "acquireRetryAttempts" ) ? dsConfig.intNumber( "acquireRetryAttempts" ) : 10 );
            ds.setAcquireRetryDelayInMs( dsConfig.has( "acquireRetryDelay" ) ? dsConfig.milliseconds( "acquireRetryDelay" ) : 1000 );
            ds.setConnectionTimeoutInMs( dsConfig.has( "connectionTimeout" ) ? dsConfig.milliseconds( "connectionTimeout" ) : 1000 );
            ds.setIdleMaxAgeInSeconds( dsConfig.has( "idleMaxAge" ) ? dsConfig.seconds( "idleMaxAge" ) : 600 );
            ds.setMaxConnectionAgeInSeconds( dsConfig.has( "maxConnectionAge" ) ? dsConfig.seconds( "maxConnectionAge" ) : 3600 );
            ds.setDisableJMX( dsConfig.has( "disableJMX" ) ? dsConfig.bool( "disableJMX" ) : true );
            ds.setStatisticsEnabled( dsConfig.has( "statisticsEnabled" ) ? dsConfig.bool( "statisticsEnabled" ) : false );
            ds.setIdleConnectionTestPeriodInSeconds( dsConfig.has( "idleConnectionTestPeriod" ) ? dsConfig.seconds( "idleConnectionTestPeriod" ) : 60 );
            ds.setDisableConnectionTracking( dsConfig.has( "disableConnectionTracking" ) ? dsConfig.bool( "disableConnectionTracking" ) : true );
            if( dsConfig.has( "initSQL" ) )
            {
                ds.setInitSQL( dsConfig.string( "initSQL" ) );
            }
            if( dsConfig.has( "logStatements" ) )
            {
                ds.setLogStatementsEnabled( dsConfig.bool( "logStatements" ) );
            }
            if( dsConfig.has( "connectionTestStatement" ) )
            {
                ds.setConnectionTestStatement( dsConfig.string( "connectionTestStatement" ) );
            }

            // JNDI
            if( dsConfig.has( "jndiName" ) )
            {
                String jndiName = dsConfig.string( "jndiName" );
                new InitialContext().rebind( jndiName, ds );
            }

            return ds;
        }
        catch( ClassNotFoundException | IllegalAccessException | InstantiationException |
               SQLException | NamingException ex )
        {
            throw new ActivationException( "JDBC Plugin unable to create '" + dsName + "' DataSource", ex );
        }
    }
}
