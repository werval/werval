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
import java.util.Collections;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.qiweb.api.Application;

import static org.qiweb.api.Application.Mode.DEV;
import static org.qiweb.api.Application.Mode.TEST;

public class Global
    extends org.qiweb.api.Global
{

    @Override
    public void onStart( Application application )
    {
        // Persistence
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(
            application.config().string( "app.persistence-unit-name" ),
            Collections.singletonMap( "eclipselink.classloader", application.classLoader() ) );
        application.metaData().put( "emf", emf );

        // Jackson JSON
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure( MapperFeature.DEFAULT_VIEW_INCLUSION, false );
        mapper.setPropertyNamingStrategy( PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES );
        application.metaData().put( "mapper", mapper );

        if( application.mode() == DEV )
        {
            createFixtures( emf );
        }
    }

    @Override
    public void onStop( Application application )
    {
        // Persistence
        EntityManagerFactory emf = application.metaData().get( EntityManagerFactory.class, "emf" );
        application.metaData().remove( "emf" );
        if( application.mode() == TEST )
        {
            dropData( emf );
        }
        emf.close();

        // Hypertext Application Language
        application.metaData().remove( "hal" );
    }

    private void createFixtures( EntityManagerFactory emf )
    {
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
            }
            em.getTransaction().commit();
        }
        finally
        {
            em.close();
        }
    }

    private void dropData( EntityManagerFactory emf )
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            List<Beer> beers = em.createQuery( "select b from Beer b", Beer.class ).getResultList();
            for( Beer beer : beers )
            {
                em.remove( beer );
            }
            List<Brewery> breweries = em.createQuery( "select b from Brewery b", Brewery.class ).getResultList();
            for( Brewery brewery : breweries )
            {
                em.remove( brewery );
            }
            em.getTransaction().commit();
        }
        finally
        {
            em.close();
        }
    }
}
