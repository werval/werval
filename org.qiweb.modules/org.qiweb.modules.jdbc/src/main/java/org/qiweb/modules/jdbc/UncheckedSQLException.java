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

import java.sql.SQLException;

/**
 * Unchecked SQLException.
 */
public class UncheckedSQLException
    extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    private final SQLException sqlEx;

    /**
     * Wrap a SQLException in an UncheckedSQLException.
     *
     * @param ex The SQLException
     */
    public UncheckedSQLException( SQLException ex )
    {
        super( ex.getMessage(), ex );
        sqlEx = ex;
    }

    /**
     * Retrieves the original SQLException.
     *
     * @return The original SQLException
     */
    public SQLException getSQLException()
    {
        return sqlEx;
    }

    /**
     * Retrieves the exception chained to this SQLException object by setNextException(SQLException ex).
     *
     * @return The next SQLException object in the chain; null if there are none
     *
     * @see SQLException#setNextException(java.sql.SQLException)
     */
    public SQLException getNextException()
    {
        return sqlEx.getNextException();
    }

    /**
     * Retrieves the SQLState for this SQLException object.
     *
     * @return The SQLState value
     */
    public String getSQLState()
    {
        return sqlEx.getSQLState();
    }
}
