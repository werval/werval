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
package org.qiweb.modules.jpa;

import io.werval.runtime.routes.RoutesParserProvider;
import io.werval.test.WervalHttpRule;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.modules.jdbc.JDBC;

import static com.jayway.restassured.RestAssured.expect;
import static io.werval.api.mime.MimeTypes.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

/**
 * JPA Plugin Test.
 */
public class JPAPluginTest
{
    @ClassRule
    public static final WervalHttpRule WERVAL = new WervalHttpRule( new RoutesParserProvider(
        "GET /directUsage org.qiweb.modules.jpa.Controller.directUsage\n"
        + "GET /withTransaction org.qiweb.modules.jpa.Controller.withTransaction\n"
        + "GET /transactional org.qiweb.modules.jpa.Controller.transactional\n"
        + "GET /multithreadedDirectUsage org.qiweb.modules.jpa.Controller.multithreadedDirectUsage\n"
        + "GET /multithreadedWithTransaction org.qiweb.modules.jpa.Controller.multithreadedWithTransaction\n"
        + "GET /multithreadedTransactional org.qiweb.modules.jpa.Controller.multithreadedTransactional\n"
        + "GET /metrics org.qiweb.modules.metrics.Tools.metrics\n"
    ) );

    @BeforeClass
    public static void setupDatabaseSchemas()
        throws SQLException
    {
        // Create schema using plain JDBC, you'll want to use something like Liquibase or Flyway in a real application
        JDBC jdbc = WERVAL.application().plugin( JDBC.class );

        // Default PU
        String createFooTable = "CREATE TABLE FOOENTITY (ID bigint AUTO_INCREMENT, NAME varchar(255), PRIMARY KEY (ID));";
        try( Connection connection = jdbc.connection();
             PreparedStatement statement = connection.prepareStatement( createFooTable ) )
        {
            statement.execute();
        }

        // Another PU
        String createBarTable = "CREATE TABLE BARENTITY (ID bigint AUTO_INCREMENT, NAME varchar(255), PRIMARY KEY (ID));";
        try( Connection connection = jdbc.connection( "another" );
             PreparedStatement statement = connection.prepareStatement( createBarTable ) )
        {
            statement.execute();
        }
    }

    @AfterClass
    public static void assertMetrics()
    {
        expect()
            .statusCode( 200 )
            .contentType( APPLICATION_JSON )
            .body( "histograms.'com.eclipse.persistence.histograms.CacheSizeBarEntity'.count", is( 3 ) )
            .body( "histograms.'com.eclipse.persistence.histograms.CacheSizeFooEntity'.count", is( 4 ) )
            .body( "timers.'com.eclipse.persistence.timers.InsertObjectQuery'.count", is( 7 ) )
            .body( "meters.'com.eclipse.persistence.meters.ClientSessionCreates'.count", is( 8 ) )
            .body( "meters.'com.eclipse.persistence.meters.ClientSessionReleases'.count", is( 8 ) )
            .body( "meters.'com.eclipse.persistence.meters.CacheHits'.count", is( 6 ) )
            .body( "meters.'com.eclipse.persistence.meters.ConnectCalls'.count", is( 4 ) )
            .body( "meters.'com.eclipse.persistence.meters.InsertObjectQuery'.count", is( 7 ) )
            .body( "meters.'com.eclipse.persistence.meters.InsertObjectQuery:org.qiweb.modules.jpa.BarEntity'.count", is( 3 ) )
            .body( "meters.'com.eclipse.persistence.meters.InsertObjectQuery:org.qiweb.modules.jpa.FooEntity'.count", is( 4 ) )
            .body( "meters.'com.eclipse.persistence.meters.ReadObjectQuery'.count", is( 6 ) )
            .body( "meters.'com.eclipse.persistence.meters.ReadObjectQuery:org.qiweb.modules.jpa.BarEntity:readBarEntity'.count", is( 3 ) )
            .body( "meters.'com.eclipse.persistence.meters.ReadObjectQuery:org.qiweb.modules.jpa.BarEntity:readBarEntity:CacheHits'.count", is( 3 ) )
            .body( "meters.'com.eclipse.persistence.meters.ReadObjectQuery:org.qiweb.modules.jpa.FooEntity:readFooEntity'.count", is( 3 ) )
            .body( "meters.'com.eclipse.persistence.meters.ReadObjectQuery:org.qiweb.modules.jpa.FooEntity:readFooEntity:CacheHits'.count", is( 3 ) )
            .body( "meters.'com.eclipse.persistence.meters.UnitOfWorkCreates'.count", is( 8 ) )
            .body( "meters.'com.eclipse.persistence.meters.UnitOfWorkReleases'.count", is( 8 ) )
            .body( "meters.'com.eclipse.persistence.meters.UnitOfWorkCommits'.count", is( 10 ) )
            .body( "meters.'com.eclipse.persistence.meters.ValueReadQuery'.count", is( 7 ) )
            .body( "meters.'com.eclipse.persistence.meters.ValueReadQuery:SEQ_GEN_IDENTITY'.count", is( 7 ) )
            .when()
            .get( "/metrics" );
    }

    @Test
    public void multiplePersistenceUnits()
    {
        JPA jpa = WERVAL.application().plugin( JPA.class );

        assertThat( jpa.emf(), equalTo( jpa.emf( "default" ) ) );
        assertThat( jpa.emf( "another" ), notNullValue() );

        try
        {
            jpa.emf( "do not exists" );
        }
        catch( PersistenceException expected )
        {
        }
    }

    @Test
    public void outOfContextDirectUsage()
    {
        // Use JPA
        JPA jpa = WERVAL.application().plugin( JPA.class );
        EntityManager em = jpa.newEntityManager();
        try
        {
            em.getTransaction().begin();
            FooEntity foo = new FooEntity( "FOO" );
            em.persist( foo );
            em.getTransaction().commit();

            Long id = foo.getId();

            em.getTransaction().begin();
            foo = em.find( FooEntity.class, id );
            assertThat( foo.getName(), equalTo( "FOO" ) );
            em.getTransaction().commit();
        }
        finally
        {
            em.close();
        }
    }

    @Test
    public void outOfContextWithTransaction()
    {
        // Use JPA.withTransaction
        JPA jpa = WERVAL.application().plugin( JPA.class );
        try
        {
            Long id = jpa.supplyWithReadWriteTx(
                "another",
                (em) ->
                {
                    System.out.println( "WILL PERSIST" );
                    BarEntity bar = new BarEntity( "BAR" );
                    em.persist( bar );
                    em.flush();
                    System.out.println( "HAS PERSISTED: " + bar.getId() );
                    return bar.getId();
                }
            );
            String name = jpa.supplyWithReadOnlyTx(
                "another",
                (em) -> em.find( BarEntity.class, id ).getName()
            );
            assertThat( name, equalTo( "BAR" ) );
        }
        finally
        {
            jpa.em( "another" ).close();
        }
    }

    @Test
    public void inContextDirectUsage()
    {
        expect()
            .statusCode( 200 )
            .body( containsString( "FOO" ) )
            .when()
            .get( "/directUsage" );
    }

    @Test
    public void inContextWithTransaction()
    {
        expect()
            .statusCode( 200 )
            .body( containsString( "BAR" ) )
            .when()
            .get( "/withTransaction" );
    }

    @Test
    public void inContextTransactional()
    {
        expect()
            .statusCode( 200 )
            .body( containsString( "FOO" ) )
            .when()
            .get( "/transactional" );
    }

    @Test
    public void multithreadedContextDirectUsage()
    {
        expect()
            .statusCode( 200 )
            .body( containsString( "FOO" ) )
            .when()
            .get( "/multithreadedDirectUsage" );
    }

    @Test
    public void multithreadedContextWithTransaction()
    {
        expect()
            .statusCode( 200 )
            .body( containsString( "BAR" ) )
            .when()
            .get( "/multithreadedWithTransaction" );
    }

    @Test
    public void multithreadedContextTransactional()
    {
        expect()
            .statusCode( 200 )
            .body( containsString( "FOO" ) )
            .when()
            .get( "/multithreadedTransactional" );
    }
}
