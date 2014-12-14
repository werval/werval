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

import io.werval.api.outcomes.Outcome;
import java.sql.*;

import static io.werval.api.context.CurrentContext.*;

public class DocumentationSupport
{
    public class SomeController
    {
        public Outcome someAction()
            throws SQLException
        {
            String result = null;
            try( Connection connection = plugin( JDBC.class ).connection() )
            {
                // Use the Connection to do whatever you need to
            }
            return outcomes().ok( result ).build();
        }
    }

    public static void specificDataSource()
        throws SQLException
    {
        try( Connection connection = plugin( JDBC.class ).connection( "second_ds" ) )
        {
            // Use the Connection to second_ds DataSource to do whatever you need to
        }
    }
}
