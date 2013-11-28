/**
 * Copyright (c) 2013 the original author or authors
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
package beerdb;

import beerdb.entities.Beer;
import beerdb.entities.Brewery;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.qiweb.api.Application;
import org.qiweb.api.exceptions.QiWebException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qiweb.api.Application.Mode.TEST;
import static org.qiweb.api.util.Strings.EMPTY;

/**
 * Beer Database Global Object.
 * <p>Apply database changelog.</p>
 * <p>Setup JPA and Jackson.</p>
 * <p>Behaviour depends on Application Mode:</p>
 * <ul>
 *     <li>PROD: Insert initial data on start.</li>
 *     <li>DEV: Insert initial data on start.</li>
 *     <li>TEST: No initial data inserted. All data dropped on stop.</li>
 * </ul>
 */
public class Global
    extends org.qiweb.api.Global
{

    private static final Logger LOG = LoggerFactory.getLogger( Global.class );

    @Override
    public void onActivate( Application application )
    {
        // Database schema migration
        liquibaseUpdate( application );

        // Persistence
        application.metaData().put( "emf", createEntityManagerFactory( application ) );
        insertInitialData( application );

        // Jackson JSON
        application.metaData().put( "mapper", createObjectMapper() );

        LOG.info( "Beer Database Activated" );
    }

    @Override
    public void onPassivate( Application application )
    {
        // Persistence
        EntityManagerFactory emf = application.metaData().get( EntityManagerFactory.class, "emf" );
        application.metaData().remove( "emf" );
        if( application.mode() == TEST )
        {
            // Drop data on TEST mode
            liquibaseDropAll( application );
        }
        emf.close();

        // Jackson JSON
        application.metaData().remove( "mapper" );

        LOG.info( "Beer Database Passivated" );
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
        Class.forName( application.config().string( "jdbc.driver" ) );
        Connection connection = DriverManager.getConnection(
            application.config().string( "jdbc.url" ),
            application.config().string( "jdbc.user" ),
            application.config().string( "jdbc.password" ) );
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation( new JdbcConnection( connection ) );
        String changelog = "beerdb/changelog/db.changelog-master.xml";
        ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor( application.classLoader() );
        Liquibase liquibase = new Liquibase( changelog, resourceAccessor, database );
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

    private EntityManagerFactory createEntityManagerFactory( Application application )
    {
        Map<String, Object> jpaProps = new HashMap<>();
        jpaProps.put( "javax.persistence.jdbc.driver", application.config().string( "jdbc.driver" ) );
        jpaProps.put( "javax.persistence.jdbc.url", application.config().string( "jdbc.url" ) );
        jpaProps.put( "javax.persistence.jdbc.user", application.config().string( "jdbc.user" ) );
        jpaProps.put( "javax.persistence.jdbc.password", application.config().string( "jdbc.password" ) );
        jpaProps.put( "eclipselink.connection-pool.default.max", application.config().string( "jpa.pool.max" ) );
        jpaProps.put( "eclipselink.classloader", application.classLoader() );
        return Persistence.createEntityManagerFactory( application.config().string( "jpa.pu_name" ), jpaProps );
    }

    private ObjectMapper createObjectMapper()
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure( MapperFeature.DEFAULT_VIEW_INCLUSION, false );
        mapper.setPropertyNamingStrategy( PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES );
        return mapper;
    }

    private void insertInitialData( Application application )
    {
        EntityManagerFactory emf = application.metaData().get( EntityManagerFactory.class, "emf" );
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            if( em.createQuery( "select b from Brewery b", Brewery.class ).getResultList().isEmpty() )
            {
                Brewery kroBrewery = new Brewery();
                {
                    kroBrewery.setName( "Kronenbourg 1664" );
                    kroBrewery.setUrl( "http://kronenbourg1664.com/" );
                    kroBrewery.setDescription(
                        "**Kronenbourg Brewery** (Brasseries Kronenbourg) is a brewery founded in 1664 by Geronimus "
                        + "Hatt in Strasbourg (at the time a Free Imperial City of the Holy Roman Empire; now France) "
                        + "as the Hatt Brewery. The name comes from the area (Cronenbourg) where the brewery relocated "
                        + "in 1850. The company is owned by the Carlsberg Group. The main brand is Kronenbourg 1664, a "
                        + "5.5% abv pale lager which is the best selling premium lager brand in France." );
                }
                Beer kro = new Beer();
                {
                    kro.setName( "Kronenbourg" );
                    kro.setAbv( 5.5F );
                    kro.setDescription( "Pale lager first brewed in 1952." );
                    kroBrewery.addBeer( kro );
                }
                Beer brown1664 = new Beer();
                {
                    brown1664.setName( "Kronenbourg 1664" );
                    brown1664.setAbv( 5F );
                    brown1664.setDescription( "" );
                    kroBrewery.addBeer( brown1664 );
                }
                Beer singleMalt = new Beer();
                {
                    singleMalt.setName( "Single Malt " );
                    singleMalt.setAbv( 6.1F );
                    singleMalt.setDescription( "French name Malt d'Exception." );
                    kroBrewery.addBeer( singleMalt );
                }

                Brewery duyckBrewery = new Brewery();
                {
                    duyckBrewery.setName( "Duyck" );
                    duyckBrewery.setUrl( "http://www.jenlain.fr/" );
                    duyckBrewery.setDescription(
                        "Since 1922, four generations have successively taken over the brewery, over its eventful "
                        + "history... From Léon to Raymond Duyck, discover the fascinating story of this devoted "
                        + "family!" );
                }
                Beer jenlainTenebreuse = new Beer();
                {
                    jenlainTenebreuse.setName( "Jenlain Ténébreuse" );
                    jenlainTenebreuse.setAbv( 7F );
                    jenlainTenebreuse.setDescription( "A GENTLE RAY OF LIGHT AMIDST THE GLOOM." );
                    duyckBrewery.addBeer( jenlainTenebreuse );
                }
                Beer jenlainAmbree = new Beer();
                {
                    jenlainAmbree.setName( "Jenlain Ambrée" );
                    jenlainAmbree.setAbv( 7.5F );
                    jenlainAmbree.setDescription( "GUARDIAN OF OUR TRADITIONS." );
                    duyckBrewery.addBeer( jenlainAmbree );
                }
                Beer jenlainBlonde = new Beer();
                {
                    jenlainBlonde.setName( "Jenlain Blonde" );
                    jenlainBlonde.setAbv( 7.5F );
                    jenlainBlonde.setDescription( "THE WORTHY HEIR." );
                    duyckBrewery.addBeer( jenlainBlonde );
                }
                Beer jenlainBlondeAbbaye = new Beer();
                {
                    jenlainBlondeAbbaye.setName( "Jenlain Blonde d'Abbaye" );
                    jenlainBlondeAbbaye.setAbv( 6.8F );
                    jenlainBlondeAbbaye.setDescription( "TIME FOR SHARING." );
                    duyckBrewery.addBeer( jenlainBlondeAbbaye );
                }
                Beer jenlainN6 = new Beer();
                {
                    jenlainN6.setName( "Jenlain N°6" );
                    jenlainN6.setAbv( 6F );
                    jenlainN6.setDescription( "PERFECTLY BALANCED NUMBER." );
                    duyckBrewery.addBeer( jenlainN6 );
                }
                Beer jenlainOr = new Beer();
                {
                    jenlainOr.setName( "Jenlain Or" );
                    jenlainOr.setAbv( 8F );
                    jenlainOr.setDescription( "THE BREWERY'S MOST PRECIOUS TREASURE." );
                    duyckBrewery.addBeer( jenlainOr );
                }
                Beer jenlainArdente = new Beer();
                {
                    jenlainArdente.setName( "Jenlain Ardente" );
                    jenlainArdente.setAbv( 3.1F );
                    jenlainArdente.setDescription( "THE FRUITS OF IMAGINATION." );
                    duyckBrewery.addBeer( jenlainArdente );
                }
                Beer jenlainBlanche = new Beer();
                {
                    jenlainBlanche.setName( "Jenlain Blanche" );
                    jenlainBlanche.setAbv( 4.3F );
                    jenlainBlanche.setDescription( "INSTANTANEOUS FRESHNESS." );
                    duyckBrewery.addBeer( jenlainBlanche );
                }

                em.persist( kroBrewery );
                em.persist( kro );
                em.persist( brown1664 );
                em.persist( singleMalt );

                em.persist( duyckBrewery );
                em.persist( jenlainTenebreuse );
                em.persist( jenlainAmbree );
                em.persist( jenlainBlonde );
                em.persist( jenlainBlondeAbbaye );
                em.persist( jenlainN6 );
                em.persist( jenlainOr );
                em.persist( jenlainArdente );
                em.persist( jenlainBlanche );

                em.getTransaction().commit();
                LOG.info( "Initial Data Inserted" );
            }
        }
        finally
        {
            em.close();
        }
    }

}
