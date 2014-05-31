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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.modules.jdbc.JDBC;
import org.qiweb.test.QiWebRule;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * JPA Plugin Test.
 */
// TODO Unit test @Transactional
public class JPAPluginTest
{
    @ClassRule
    public static final QiWebRule QIWEB = new QiWebRule();

    @Test
    public void persistenceUnitsSetup()
    {
        JPA jpa = QIWEB.application().plugin( JPA.class );

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
    public void actualUsage()
        throws SQLException
    {
        // Create schema using plain JDBC, you'll want to use something like Liquibase or Flyway in a real application
        JDBC jdbc = QIWEB.application().plugin( JDBC.class );
        String createTable = "CREATE TABLE FOOENTITY (ID bigint AUTO_INCREMENT, NAME varchar(255), PRIMARY KEY (ID));";
        try( Connection connection = jdbc.connection();
             PreparedStatement statement = connection.prepareStatement( createTable ) )
        {
            statement.execute();
        }

        // Use JPA
        JPA jpa = QIWEB.application().plugin( JPA.class );
        EntityManager em = jpa.em();
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
    public void withTransaction()
        throws SQLException
    {
        // Create schema using plain JDBC, you'll want to use something like Liquibase or Flyway in a real application
        JDBC jdbc = QIWEB.application().plugin( JDBC.class );
        String createTable = "CREATE TABLE BARENTITY (ID bigint AUTO_INCREMENT, NAME varchar(255), PRIMARY KEY (ID));";
        try( Connection connection = jdbc.connection( "another" );
             PreparedStatement statement = connection.prepareStatement( createTable ) )
        {
            statement.execute();
        }

        // Use JPA.withTransaction
        JPA jpa = QIWEB.application().plugin( JPA.class );
        jpa.withReadWriteTx(
            "another",
            (EntityManager em) ->
            {
                System.out.println( "WILL PERSIST" );
                BarEntity bar = new BarEntity( "BAR" );
                em.persist( bar );
                System.out.println( "HAS PERSISTED: " + bar.getId() );
            }
        );
        int count = jpa.withReadOnlyTx(
            "another",
            (EntityManager em) -> em.createQuery( "from BarEntity b" ).getResultList().size()
        );
        assertThat( count, is( 1 ) );
    }
}
