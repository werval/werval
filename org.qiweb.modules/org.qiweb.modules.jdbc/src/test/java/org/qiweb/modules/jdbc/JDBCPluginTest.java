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

import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.test.QiWebRule;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * JDBCPluginTest.
 */
public class JDBCPluginTest
{
    @ClassRule
    public static final QiWebRule QIWEB = new QiWebRule();

    @Test
    public void dataSourcesSetup()
    {
        JDBC jdbc = QIWEB.application().plugin( JDBC.class );
        assertThat( jdbc.dataSource(), equalTo( jdbc.dataSource( "default" ) ) );
        assertThat( jdbc.dataSource( "another" ), notNullValue() );
    }

    @Test
    public void dataSourceUsage()
        throws SQLException
    {
        DataSource dataSource = QIWEB.application().plugin( JDBC.class ).dataSource();
        try( Connection connection = dataSource.getConnection() )
        {
            connection.getMetaData().getTypeInfo();
        }
    }

    @Test
    public void connectionUsage()
        throws SQLException
    {
        try( Connection connection = QIWEB.application().plugin( JDBC.class ).connection() )
        {
            connection.getMetaData().getTypeInfo();
        }
    }

    @Test
    public void jndiLookup()
        throws NamingException
    {
        Object ds = new InitialContext().lookup( "defaultDS" );
        assertThat( ds, notNullValue() );
        assertThat( ds, instanceOf( DataSource.class ) );
    }
}
