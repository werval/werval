/*
 * Copyright (c) 2013 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package beerdb;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.operation.Operation;
import java.sql.Connection;
import java.util.Arrays;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.validation.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.qiweb.modules.jpa.JPA;
import org.qiweb.test.QiWebHttpTest;

import static com.jayway.restassured.RestAssured.expect;
import static com.ninja_squad.dbsetup.Operations.deleteAllFrom;
import static com.ninja_squad.dbsetup.Operations.insertInto;
import static com.ninja_squad.dbsetup.Operations.sequenceOf;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.qiweb.api.mime.MimeTypesNames.APPLICATION_JSON;

public class DbSetupTest
    extends QiWebHttpTest
{
    private static final Operation DELETE_ALL = deleteAllFrom( "beers", "breweries" );
    private static final Operation INSERT_CHIMAY = sequenceOf(
        insertInto( "breweries" )
        .columns( "id", "name", "url", "since", "description", "beers_count" )
        .values( 100L, "Chimay", "http://chimay.com", 1847, "The chimay brewery tagada tagada tsoin tsoin.", 2 )
        .build(),
        insertInto( "beers" )
        .columns( "id", "name", "abv", "description", "brewery_id" )
        .values( 100L, "Blue Chimay", 9.2F, "The blue chimay is damn good.", 100L )
        .values( 200L, "Red Chimay", 8.5F, "That's the way I like it.", 100L )
        .build()
    );

    @Before
    public void before()
    {
        EntityManagerFactory emf = application().plugin( JPA.class ).emf();
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            new DbSetup( () -> em.unwrap( Connection.class ), INSERT_CHIMAY ).launch();
            // DbSetup commit the transaction under the hood ...
        }
        catch( ConstraintViolationException ex )
        {
            System.err.println(
                "ConstraintViolationException: "
                + Arrays.toString( ex.getConstraintViolations().toArray() )
            );
            throw ex;
        }
        finally
        {
            em.close();
        }
    }

    @Test
    public void chimay()
    {
        // Find Chimay Brewery
        expect()
            .statusCode( 200 )
            .contentType( APPLICATION_JSON )
            .body( "", hasSize( 3 ) )
            .body( "name", hasItems( "Chimay" ) )
            .when()
            .get( "/api/breweries" );

        // Find Chimay Beers
        expect()
            .statusCode( 200 )
            .contentType( APPLICATION_JSON )
            .body( "", hasSize( 13 ) )
            .body( "name", hasItems( "Blue Chimay", "Red Chimay" ) )
            .when()
            .get( "/api/beers" );
    }
}
