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
                Brewery kroBrewery = Brewery.newBrewery( "Kronenbourg", "http://kronenbourg1664.com/" );
                Beer whiteBeer = Beer.newBeer( kroBrewery, "Kro Blanche", 3 );
                Beer blondBeer = Beer.newBeer( kroBrewery, "Kro Blonde", 3 );
                Beer brownBeer = Beer.newBeer( kroBrewery, "Kro Brune", 3 );
                em.persist( kroBrewery );
                em.persist( whiteBeer );
                em.persist( blondBeer );
                em.persist( brownBeer );
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
