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
package io.werval.modules.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.werval.api.Application;
import io.werval.api.Config;
import io.werval.api.Plugin;
import io.werval.api.exceptions.ActivationException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import io.werval.modules.jndi.JNDI;
import io.werval.modules.metrics.Metrics;

import static io.werval.util.Strings.EMPTY;
import static io.werval.util.Strings.isEmpty;
import static io.werval.util.Strings.join;

/**
 * JDBC Plugin that manage DataSources using HikariCP pools.
 */
public class JDBCPlugin
    implements Plugin<JDBC>
{
    private static final String DEFAULT_DATASOURCE = "jdbc.default_datasource";
    private static final String DATASOURCES = "jdbc.datasources";
    private static final String METRICS = "jdbc.metrics";
    private static final String LOG4JDBC_DRIVER = "net.sf.log4jdbc.sql.jdbcapi.DriverSpy";
    private JDBC jdbc;

    @Override
    public Class<JDBC> apiType()
    {
        return JDBC.class;
    }

    @Override
    public List<Class<?>> dependencies( Config config )
    {
        List<Class<?>> deps = new ArrayList<>();
        if( config.has( DATASOURCES ) )
        {
            Config allDsConfig = config.object( DATASOURCES );
            for( String dsName : allDsConfig.subKeys() )
            {
                if( allDsConfig.object( dsName ).has( "jndiName" ) )
                {
                    // At least one DataSource has to be registered in JNDI, depend on the JNDI plugin
                    deps.add( JNDI.class );
                    break;
                }
            }
        }
        if( config.bool( METRICS ) )
        {
            deps.add( Metrics.class );
        }
        return deps;
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
            setupLog4Jdbc( application, allDsConfig );
            for( String dsName : allDsConfig.subKeys() )
            {
                Config dsConfig = allDsConfig.object( dsName );
                HikariDataSource ds = createDataSource( dsName, dsConfig, application, config.bool( METRICS ) );
                dataSources.put( dsName, ds );
            }
        }
        jdbc = new JDBC( dataSources, config.string( DEFAULT_DATASOURCE ) );
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

    private void setupLog4Jdbc( Application application, Config allDsConfig )
    {
        // Load log4jdbc properties from application configuration
        Map<String, String> globalProperties = new HashMap<>();
        if( application.config().has( "jdbc.log4jdbc" ) )
        {
            application.config().stringMap( "jdbc.log4jdbc" ).forEach(
                (key, value) ->
                {
                    globalProperties.put( "log4jdbc." + key, value );
                }
            );
        }
        // Load used drivers from datasources configuration
        Set<String> log4jdbcEnabledDrivers = new LinkedHashSet<>();
        for( String dsName : allDsConfig.subKeys() )
        {
            Config dsConfig = allDsConfig.object( dsName );
            if( dsConfig.has( "log4jdbc" ) && dsConfig.bool( "log4jdbc" ) )
            {
                log4jdbcEnabledDrivers.add( dsConfig.string( "driver" ) );
            }
        }
        // Apply if appropriate
        if( !log4jdbcEnabledDrivers.isEmpty() )
        {
            System.setProperty( "log4jdbc.drivers", join( log4jdbcEnabledDrivers, "," ) );
            globalProperties.forEach(
                (key, value) ->
                {
                    System.setProperty( key, value );
                }
            );
        }
    }

    private HikariDataSource createDataSource( String dsName, Config dsConfig, Application app, boolean metrics )
    {
        // JDBC configuration
        boolean log4jdbc = dsConfig.has( "log4jdbc" ) && dsConfig.bool( "log4jdbc" );
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
        if( log4jdbc )
        {
            url = url.substring( 0, 5 ) + "log4jdbc:" + url.substring( 5 );
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
            // Load Database Driver Explicitely
            DriverManager.registerDriver(
                (Driver) Class.forName( driver, true, app.classLoader() ).newInstance()
            );
            if( log4jdbc )
            {
                // Load log4jdbc Driver Explicitely
                DriverManager.registerDriver(
                    (Driver) Class.forName( LOG4JDBC_DRIVER, true, app.classLoader() ).newInstance()
                );
            }
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setPoolName( dsName );
            hikariConfig.setDriverClassName( log4jdbc ? LOG4JDBC_DRIVER : driver );
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

            // Metrics
            if( metrics )
            {
                hikariConfig.setMetricRegistry( app.plugin( Metrics.class ).metrics() );
            }

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
