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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.qiweb.api.Application;
import org.qiweb.api.Config;
import org.qiweb.api.Plugin;
import org.qiweb.api.exceptions.ActivationException;
import org.qiweb.modules.jndi.JNDI;

import static java.util.Collections.EMPTY_LIST;
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
    private JDBC jdbc;

    @Override
    public Class<JDBC> apiType()
    {
        return JDBC.class;
    }

    @Override
    public List<Class<?>> dependencies( Config config )
    {
        if( config.has( DATASOURCES ) )
        {
            Config allDsConfig = config.object( DATASOURCES );
            for( String dsName : allDsConfig.subKeys() )
            {
                if( allDsConfig.object( dsName ).has( "jndiName" ) )
                {
                    // At least one DataSource has to be registered in JNDI, depend on the JNDI plugin
                    return Arrays.asList( JNDI.class );
                }
            }
        }
        return EMPTY_LIST;
    }

    @Override
    public JDBC api()
    {
        return jdbc;
    }

    @Override
    public void onActivate( Application application )
        throws ActivationException
    {
        Config config = application.config();
        Map<String, HikariDataSource> dataSources = new HashMap<>();
        if( config.has( DATASOURCES ) )
        {
            Config allDsConfig = config.object( DATASOURCES );
            for( String dsName : allDsConfig.subKeys() )
            {
                Config dsConfig = allDsConfig.object( dsName );
                HikariDataSource ds = createDataSource( dsName, dsConfig, application.classLoader() );
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

    private HikariDataSource createDataSource( String dsName, Config dsConfig, ClassLoader loader )
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

        // Setup DataSource
        try
        {
            DriverManager.registerDriver( (Driver) Class.forName( driver, true, loader ).newInstance() );
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setPoolName( dsName );
            hikariConfig.setDriverClassName( driver );
            hikariConfig.setJdbcUrl( url );
            hikariConfig.setUsername( user );
            hikariConfig.setPassword( password );
            if( dsConfig.has( "autocommit" ) )
            {
                hikariConfig.setAutoCommit( dsConfig.bool( "autocommit" ) );
            }
            if( dsConfig.has( "isolation" ) )
            {
                hikariConfig.setTransactionIsolation( dsConfig.string( "isolation" ) );
            }
            if( dsConfig.has( "readOnly" ) )
            {
                hikariConfig.setReadOnly( dsConfig.bool( "readOnly" ) );
            }
            if( dsConfig.has( "catalog" ) )
            {
                hikariConfig.setCatalog( dsConfig.string( "catalog" ) );
            }

            // Pool configuration
            if( dsConfig.has( "minimumIdle" ) )
            {
                hikariConfig.setMinimumIdle( dsConfig.intNumber( "minimumIdle" ) );
            }
            if( dsConfig.has( "maximumPoolSize" ) )
            {
                hikariConfig.setMaximumPoolSize( dsConfig.intNumber( "maximumPoolSize" ) );
            }
            if( dsConfig.has( "connectionTimeout" ) )
            {
                hikariConfig.setConnectionTimeout( dsConfig.milliseconds( "connectionTimeout" ) );
            }
            if( dsConfig.has( "idleTimeout" ) )
            {
                hikariConfig.setIdleTimeout( dsConfig.milliseconds( "idleTimeout" ) );
            }
            if( dsConfig.has( "maxLifetime" ) )
            {
                hikariConfig.setMaxLifetime( dsConfig.milliseconds( "maxLifetime" ) );
            }
            if( dsConfig.has( "initializationFailFast" ) )
            {
                hikariConfig.setInitializationFailFast( dsConfig.bool( "initializationFailFast" ) );
            }
            if( dsConfig.has( "leakDetectionThreshold" ) )
            {
                hikariConfig.setLeakDetectionThreshold( dsConfig.milliseconds( "leakDetectionThreshold" ) );
            }
            if( dsConfig.has( "connectionInitSql" ) )
            {
                hikariConfig.setConnectionInitSql( dsConfig.string( "connectionInitSql" ) );
            }
            if( dsConfig.has( "jdbc4ConnectionTest" ) )
            {
                hikariConfig.setJdbc4ConnectionTest( dsConfig.bool( "jdbc4ConnectionTest" ) );
            }
            if( dsConfig.has( "connectionTestQuery" ) )
            {
                hikariConfig.setConnectionTestQuery( dsConfig.string( "connectionTestQuery" ) );
            }
            if( dsConfig.has( "registerMbeans" ) )
            {
                hikariConfig.setRegisterMbeans( dsConfig.bool( "registerMbeans" ) );
            }
            if( dsConfig.has( "isolateInternalQueries" ) )
            {
                hikariConfig.setIsolateInternalQueries( dsConfig.bool( "isolateInternalQueries" ) );
            }
            // TODO JDBC HikariCP Metrics
            // See https://github.com/brettwooldridge/HikariCP/issues/112
            // hikariConfig.setMetricsTrackerClassName( "IMetricsTracker class name");

            HikariDataSource hds = new HikariDataSource( hikariConfig );

            // JNDI
            if( dsConfig.has( "jndiName" ) )
            {
                String jndiName = dsConfig.string( "jndiName" );
                new InitialContext().rebind( jndiName, hds );
            }

            return hds;
        }
        catch( ClassNotFoundException | IllegalAccessException | InstantiationException |
               SQLException | NamingException ex )
        {
            throw new ActivationException( "JDBC Plugin unable to create '" + dsName + "' DataSource", ex );
        }
    }
}
