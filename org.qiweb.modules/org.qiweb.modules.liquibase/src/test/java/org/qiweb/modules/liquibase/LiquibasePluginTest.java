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
package org.qiweb.modules.liquibase;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.modules.jdbc.JDBC;
import org.qiweb.test.QiWebRule;

import static org.junit.Assert.assertTrue;

/**
 * Liquibase Plugin Test.
 */
public class LiquibasePluginTest
{
    @ClassRule
    public static final QiWebRule QIWEB = new QiWebRule();

    @Test
    public void liquibase()
        throws SQLException
    {
        try( Connection connection = QIWEB.application().plugin( JDBC.class ).connection();
             ResultSet resultSet = connection.getMetaData().getTables( null, null, "BREWERIES", null ) )
        {
            assertTrue( resultSet.next() );
        }
    }
}
