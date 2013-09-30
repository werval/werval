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

import com.theoryinpractise.halbuilder.api.RepresentationFactory;
import com.theoryinpractise.halbuilder.standard.StandardRepresentationFactory;
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

        // Hypertext Application Language
        RepresentationFactory halFactory = new StandardRepresentationFactory();
        halFactory.withFlag( RepresentationFactory.PRETTY_PRINT );
        application.metaData().put( "hal", halFactory );

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
                Brewery kroBrewery = Brewery.newBrewery(
                    "Kronenbourg 1664",
                    "http://kronenbourg1664.com/",
                    "**Kronenbourg Brewery** (Brasseries Kronenbourg) is a brewery founded in 1664 by Geronimus Hatt in "
                    + "Strasbourg (at the time a Free Imperial City of the Holy Roman Empire; now France) as the Hatt "
                    + "Brewery. The name comes from the area (Cronenbourg) where the brewery relocated in 1850. The "
                    + "company is owned by the Carlsberg Group. The main brand is Kronenbourg 1664, a 5.5% abv pale "
                    + "lager which is the best selling premium lager brand in France." );
                Beer kro = Beer.newBeer( kroBrewery, "Kronenbourg", 5.5F, "Pale lager first brewed in 1952." );
                Beer brown1664 = Beer.newBeer( kroBrewery, "Kronenbourg 1664", 5, "" );
                Beer singleMalt = Beer.newBeer( kroBrewery, "Single Malt ", 6.1F, "French name Malt d'Exception." );

                Brewery duyckBrewery = Brewery.newBrewery(
                    "Duyck",
                    "http://www.jenlain.fr/",
                    "Since 1922, four generations have successively taken over the brewery, over its eventful "
                    + "history... From Léon to Raymond Duyck, discover the fascinating story of this devoted family!" );
                Beer jenlainTenebreuse = Beer.newBeer( duyckBrewery, "Jenlain Ténébreuse", 7, "A GENTLE RAY OF LIGHT AMIDST THE GLOOM." );
                Beer jenlainAmbree = Beer.newBeer( duyckBrewery, "Jenlain Ambrée", 7.5F, "GUARDIAN OF OUR TRADITIONS." );
                Beer jenlainBlonde = Beer.newBeer( duyckBrewery, "Jenlain Blonde", 7.5F, "THE WORTHY HEIR." );
                Beer jenlainBlondeAbbaye = Beer.newBeer( duyckBrewery, "Jenlain Blonde d'Abbaye", 6.8F, "TIME FOR SHARING." );
                Beer jenlainN6 = Beer.newBeer( duyckBrewery, "Jenlain N°6", 6F, "PERFECTLY BALANCED NUMBER." );
                Beer jenlainOr = Beer.newBeer( duyckBrewery, "Jenlain Or", 8F, "THE BREWERY'S MOST PRECIOUS TREASURE." );
                Beer jenlainArdente = Beer.newBeer( duyckBrewery, "Jenlain Ardente", 3.1F, "THE FRUITS OF IMAGINATION." );
                Beer jenlainBlanche = Beer.newBeer( duyckBrewery, "Jenlain Blanche", 4.3F, "INSTANTANEOUS FRESHNESS." );


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
