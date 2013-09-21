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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.theoryinpractise.halbuilder.api.Representation;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.qiweb.api.controllers.Outcome;

import static com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON;
import static org.qiweb.api.controllers.Controller.application;
import static org.qiweb.api.controllers.Controller.outcomes;
import static org.qiweb.api.controllers.Controller.reverseRoutes;
import static org.qiweb.api.mime.MimeTypes.APPLICATION_JSON;
import static org.qiweb.api.routes.ReverseRoutes.GET;
import static org.qiweb.samples.beerdb.BuildVersion.COMMIT;
import static org.qiweb.samples.beerdb.BuildVersion.DATE;
import static org.qiweb.samples.beerdb.BuildVersion.DETAILED_VERSION;
import static org.qiweb.samples.beerdb.BuildVersion.DIRTY;
import static org.qiweb.samples.beerdb.BuildVersion.NAME;
import static org.qiweb.samples.beerdb.BuildVersion.VERSION;

public class API
{

    private final EntityManagerFactory emf;
    private final RepresentationFactory hal;

    public API()
    {
        this.emf = application().metaData().get( EntityManagerFactory.class, "emf" );
        this.hal = application().metaData().get( RepresentationFactory.class, "hal" );
    }

    public Outcome index()
        throws JsonProcessingException
    {
        Representation resource = hal.
            newRepresentation( reverseRoutes().of( GET( API.class ).index() ).httpUrl() ).
            withLink( "beers", reverseRoutes().of( GET( API.class ).beers() ).httpUrl() ).
            withLink( "breweries", reverseRoutes().of( GET( API.class ).breweries() ).httpUrl() ).
            withProperty( "commit", COMMIT ).
            withProperty( "date", DATE ).
            withProperty( "detailed-version", DETAILED_VERSION ).
            withProperty( "dirty", DIRTY ).
            withProperty( "name", NAME ).
            withProperty( "version", VERSION );
        String json = resource.toString( HAL_JSON );
        return outcomes().ok( json ).as( APPLICATION_JSON ).build();
    }

    //
    //  _____                       _
    // | __  |___ ___ _ _ _ ___ ___|_|___ ___
    // | __ -|  _| -_| | | | -_|  _| | -_|_ -|
    // |_____|_| |___|_____|___|_| |_|___|___|
    // _________________________________________________________________________________________________________________
    //
    public Outcome breweries()
        throws JsonProcessingException
    {
        Representation resource = hal.
            newRepresentation( reverseRoutes().of( GET( API.class ).breweries() ).httpUrl() );
        EntityManager em = emf.createEntityManager();
        try
        {
            List<Brewery> breweries = em.createQuery( "select b from Brewery b", Brewery.class ).getResultList();
            resource.withProperty( "count", breweries.size() );
            for( Brewery brewery : breweries )
            {
                Representation beerResource = hal.
                    newRepresentation( reverseRoutes().of( GET( API.class ).brewery( brewery.getId() ) ).httpUrl() );
                beerResource.withBean( brewery );
                resource.withRepresentation( "brewery", beerResource );
            }
            String json = resource.toString( HAL_JSON );
            return outcomes().ok( json ).as( APPLICATION_JSON ).build();
        }
        finally
        {
            em.close();
        }
    }

    public Outcome createBrewery()
    {

        return outcomes().notImplemented().build();
    }

    public Outcome brewery( Long id )
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            Brewery brewery = em.find( Brewery.class, id );
            if( brewery == null )
            {
                return outcomes().notFound().build();
            }
            Representation resource = hal.
                newRepresentation( reverseRoutes().of( GET( API.class ).beer( id ) ).httpUrl() ).
                withBean( brewery );
            String json = resource.toString( HAL_JSON );
            return outcomes().ok( json ).as( APPLICATION_JSON ).build();
        }
        finally
        {
            em.close();
        }
    }

    public Outcome updateBrewery( Long id )
    {
        return outcomes().notImplemented().build();
    }

    public Outcome deleteBrewery( Long id )
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            Brewery brewery = em.find( Brewery.class, id );
            if( brewery == null )
            {
                return outcomes().notFound().build();
            }
            em.remove( brewery );
            em.flush();
            return outcomes().ok().build();
        }
        finally
        {
            em.close();
        }
    }

    //
    //  _____
    // | __  |___ ___ ___ ___
    // | __ -| -_| -_|  _|_ -|
    // |_____|___|___|_| |___|
    // _________________________________________________________________________________________________________________
    //
    public Outcome beers()
        throws JsonProcessingException
    {
        Representation resource = hal.
            newRepresentation( reverseRoutes().of( GET( API.class ).beers() ).httpUrl() );
        EntityManager em = emf.createEntityManager();
        try
        {
            List<Beer> beers = em.createQuery( "select b from Beer b", Beer.class ).getResultList();
            resource.withProperty( "count", beers.size() );
            for( Beer beer : beers )
            {
                Representation beerResource = hal.
                    newRepresentation( reverseRoutes().of( GET( API.class ).beer( beer.getId() ) ).httpUrl() );
                beerResource.withBean( beer );
                resource.withRepresentation( "beer", beerResource );
            }
            String json = resource.toString( HAL_JSON );
            return outcomes().ok( json ).as( APPLICATION_JSON ).build();
        }
        finally
        {
            em.close();
        }
    }

    public Outcome createBeer()
    {
        return outcomes().notImplemented().build();
    }

    public Outcome beer( Long id )
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            Beer beer = em.find( Beer.class, id );
            if( beer == null )
            {
                return outcomes().notFound().build();
            }
            Representation resource = hal.
                newRepresentation( reverseRoutes().of( GET( API.class ).beer( id ) ).httpUrl() ).
                withBean( beer );
            String json = resource.toString( HAL_JSON );
            return outcomes().ok( json ).as( APPLICATION_JSON ).build();
        }
        finally
        {
            em.close();
        }
    }

    public Outcome updateBeer( Long id )
    {
        return outcomes().notImplemented().build();
    }

    public Outcome deleteBeer( Long id )
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            Beer beer = em.find( Beer.class, id );
            if( beer == null )
            {
                return outcomes().notFound().build();
            }
            em.remove( beer );
            em.flush();
            return outcomes().ok().build();
        }
        finally
        {
            em.close();
        }
    }
}
