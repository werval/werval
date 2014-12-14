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

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import javax.sql.DataSource;

/**
 * JDBC.
 */
public class JDBC
{
    private final Map<String, HikariDataSource> dataSources;
    private final String defaultDsName;

    /* package */ JDBC( Map<String, HikariDataSource> dataSources, String defaultDsName )
    {
        this.dataSources = dataSources;
        this.defaultDsName = defaultDsName;
    }

    /**
     * Default DataSource.
     *
     * @return The default DataSource
     */
    public DataSource dataSource()
    {
        return dataSource( defaultDsName );
    }

    /**
     * Connection using the default DataSource.
     *
     * @return A Connection using the default DataSource
     *
     * @throws UncheckedSQLException if a database access error occurs
     */
    public Connection connection()
        throws UncheckedSQLException
    {
        try
        {
            return dataSource().getConnection();
        }
        catch( SQLException ex )
        {
            throw new UncheckedSQLException( ex );
        }
    }

    /**
     * DataSource.
     *
     * @param dataSourceName Name of the DataSource
     *
     * @return The DataSource named {@literal dataSourceName}
     */
    public DataSource dataSource( String dataSourceName )
    {
        if( !dataSources.containsKey( dataSourceName ) )
        {
            throw new IllegalArgumentException( "Unknown DataSource '" + dataSourceName + "'" );
        }
        return dataSources.get( dataSourceName );
    }

    /**
     * Connection using a named DataSource.
     *
     * @param dataSourceName Name of the DataSource to use
     *
     * @return A Connection using the DataSource named {@literal dataSourceName}
     *
     * @throws UncheckedSQLException if a database access error occurs
     */
    public Connection connection( String dataSourceName )
        throws UncheckedSQLException
    {
        try
        {
            return dataSource( dataSourceName ).getConnection();
        }
        catch( SQLException ex )
        {
            throw new UncheckedSQLException( ex );
        }
    }

    /* package */ void passivate()
    {
        dataSources.values().forEach( ds -> ds.close() );
        dataSources.clear();
    }
}
