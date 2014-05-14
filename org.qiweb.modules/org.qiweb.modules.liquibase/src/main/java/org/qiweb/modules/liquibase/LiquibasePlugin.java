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
import java.sql.SQLException;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.qiweb.api.Application;
import org.qiweb.api.Plugin;
import org.qiweb.api.exceptions.ActivationException;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.modules.jdbc.JDBC;

import static org.qiweb.api.Mode.TEST;
import static org.qiweb.api.util.Strings.EMPTY;

/**
 * Liquibase Plugin.
 */
public class LiquibasePlugin
    extends Plugin.Void
{
    @Override
    public void onActivate( Application application )
        throws ActivationException
    {
        String changelog = application.config().string( "liquibase.changelog" );
        if( application.classLoader().getResource( changelog ) == null )
        {
            throw new ActivationException(
                "No changelog at '" + changelog + "'. Check your liquibase.changelog configuration property."
            );
        }
        // Database schema migration
        liquibaseUpdate( application );
    }

    @Override
    public void onPassivate( Application application )
    {
        if( application.mode() == TEST && application.config().bool( "liquibase.drop_after_tests" ) )
        {
            // Drop ALL data on TEST mode
            liquibaseDropAll( application );
        }
    }

    private void liquibaseUpdate( Application application )
    {
        Liquibase liquibase = null;
        try
        {
            liquibase = newLiquibase( application );
            liquibase.update( EMPTY );
        }
        catch( ClassNotFoundException | LiquibaseException | SQLException ex )
        {
            throw new QiWebException( "Unable to apply database changelog: " + ex.getMessage(), ex );
        }
        finally
        {
            closeLiquibaseSilently( liquibase );
        }
    }

    private void liquibaseDropAll( Application application )
    {
        Liquibase liquibase = null;
        try
        {
            liquibase = newLiquibase( application );
            liquibase.dropAll();
        }
        catch( ClassNotFoundException | LiquibaseException | SQLException ex )
        {
            throw new QiWebException( "Unable to drop database data: " + ex.getMessage(), ex );
        }
        finally
        {
            closeLiquibaseSilently( liquibase );
        }
    }

    private Liquibase newLiquibase( Application application )
        throws ClassNotFoundException, SQLException, LiquibaseException
    {
        JDBC jdbc = application.plugin( JDBC.class );
        Connection connection = application.config().has( "liquibase.datasource" )
                                ? jdbc.connection( application.config().string( "liquibase.datasource" ) )
                                : jdbc.connection();
        Liquibase liquibase = new Liquibase(
            application.config().string( "liquibase.changelog" ),
            new ClassLoaderResourceAccessor( application.classLoader() ),
            DatabaseFactory.getInstance().findCorrectDatabaseImplementation( new JdbcConnection( connection ) )
        );
        return liquibase;
    }

    private void closeLiquibaseSilently( Liquibase liquibase )
    {
        if( liquibase != null )
        {
            try
            {
                liquibase.getDatabase().getConnection().close();
            }
            catch( DatabaseException ignored )
            {
            }
        }
    }
}
