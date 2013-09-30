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

import com.theoryinpractise.halbuilder.api.ReadableRepresentation;
import com.theoryinpractise.halbuilder.api.Representation;
import com.theoryinpractise.halbuilder.api.RepresentationException;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;
import java.io.StringReader;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.validation.ConstraintViolationException;
import org.qiweb.api.controllers.Outcome;

import static com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON;
import static org.qiweb.api.controllers.Controller.application;
import static org.qiweb.api.controllers.Controller.outcomes;
import static org.qiweb.api.controllers.Controller.request;
import static org.qiweb.api.controllers.Controller.reverseRoutes;
import static org.qiweb.api.http.Headers.Names.LOCATION;
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
    {
        Representation resource = hal.
            newRepresentation( reverseRoutes().of( GET( API.class ).index() ).httpUrl() ).
            withLink( "beers", reverseRoutes().of( GET( API.class ).beers() ).httpUrl() ).
            withLink( "breweries", reverseRoutes().of( GET( API.class ).breweries() ).httpUrl() ).
            withProperty( "commit", COMMIT ).
            withProperty( "date", DATE ).
            withProperty( "detailed_version", DETAILED_VERSION ).
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
    {
        Representation resource = hal.
            newRepresentation( reverseRoutes().of( GET( API.class ).breweries() ).httpUrl() ).
            withLink( "api", reverseRoutes().of( GET( API.class ).index() ).httpUrl() );
        EntityManager em = emf.createEntityManager();
        try
        {
            List<Brewery> breweries = em.createQuery( "select b from Brewery b", Brewery.class ).getResultList();
            resource.withProperty( "count", breweries.size() );
            for( Brewery brewery : breweries )
            {
                String breweryRoute = reverseRoutes().of( GET( API.class ).brewery( brewery.getId() ) ).httpUrl();
                Representation breweryResource = hal.newRepresentation( breweryRoute ).
                    withProperty( "id", brewery.getId() ).
                    withProperty( "name", brewery.getName() ).
                    withProperty( "since", brewery.getSince() ).
                    withProperty( "url", brewery.getUrl() ).
                    withProperty( "description", brewery.getDescription() );
                resource.withRepresentation( "brewery", breweryResource );
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
        if( !APPLICATION_JSON.equals( request().contentType() ) )
        {
            return outcomes().badRequest().
                withBody( "Unacceptable content-type:" + request().contentType() ).build();
        }
        String body = request().body().asString();
        String name, url, description;
        try
        {
            ReadableRepresentation input = hal.readRepresentation( new StringReader( body ) );
            name = input.getValue( "name" ).toString().trim();
            url = input.getValue( "url" ).toString().trim();
            description = input.getValue( "description" ).toString().trim();
        }
        catch( RepresentationException ex )
        {
            return outcomes().badRequest().
                withBody( ex.getMessage() ).build();
        }
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            Brewery brewery = Brewery.newBrewery( name, url, description );
            em.persist( brewery );
            em.getTransaction().commit();
            String breweryRoute = reverseRoutes().of( GET( API.class ).brewery( brewery.getId() ) ).httpUrl();
            String json = hal.newRepresentation().
                withProperty( "id", brewery.getId() ).
                withProperty( "url", breweryRoute ).
                toString( HAL_JSON );
            return outcomes().
                created().
                withHeader( LOCATION, breweryRoute ).
                as( APPLICATION_JSON ).
                withBody( json ).
                build();
        }
        catch( ConstraintViolationException ex )
        {
            return outcomes().badRequest().
                withBody( ex.getConstraintViolations().toString() ).build();
        }
        finally
        {
            em.close();
        }
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
                newRepresentation( reverseRoutes().of( GET( API.class ).brewery( id ) ).httpUrl() ).
                withLink( "api", reverseRoutes().of( GET( API.class ).index() ).httpUrl() ).
                withLink( "list", reverseRoutes().of( GET( API.class ).breweries() ).httpUrl() ).
                withProperty( "id", brewery.getId() ).
                withProperty( "name", brewery.getName() ).
                withProperty( "url", brewery.getUrl() ).
                withProperty( "description", brewery.getDescription() );
            List<Beer> beers = em.createQuery( "select b from Beer b where b.brewery=:brewery", Beer.class ).
                setParameter( "brewery", brewery ).getResultList();
            resource.withProperty( "beers-count", beers.size() );
            for( Beer beer : beers )
            {
                Representation beerResource = hal.
                    newRepresentation( reverseRoutes().of( GET( API.class ).beer( beer.getId() ) ).httpUrl() ).
                    withProperty( "id", beer.getId() ).
                    withProperty( "name", beer.getName() );
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

    public Outcome updateBrewery( Long id )
    {
        if( !APPLICATION_JSON.equals( request().contentType() ) )
        {
            return outcomes().badRequest().
                withBody( "Unacceptable content-type:" + request().contentType() ).build();
        }
        String body = request().body().asString();
        String name, url, description;
        try
        {
            ReadableRepresentation input = hal.readRepresentation( new StringReader( body ) );
            name = input.getValue( "name" ).toString().trim();
            url = input.getValue( "url" ).toString().trim();
            description = input.getValue( "description" ).toString().trim();
        }
        catch( RepresentationException ex )
        {
            return outcomes().badRequest().
                withBody( ex.getMessage() ).build();
        }
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            Brewery brewery = em.find( Brewery.class, id );
            if( brewery == null )
            {
                return outcomes().notFound().build();
            }
            brewery.setName( name );
            brewery.setUrl( url );
            brewery.setDescription( description );
            em.persist( brewery );
            em.getTransaction().commit();
            return outcomes().ok().build();
        }
        catch( ConstraintViolationException ex )
        {
            return outcomes().badRequest().
                withBody( ex.getConstraintViolations().toString() ).build();
        }
        finally
        {
            em.close();
        }
    }

    public Outcome deleteBrewery( Long id )
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            Brewery brewery = em.find( Brewery.class, id );
            if( brewery == null )
            {
                return outcomes().notFound().build();
            }
            List<Beer> beers = em.createQuery( "select b from Beer b where b.brewery=:brewery", Beer.class ).
                setParameter( "brewery", brewery ).getResultList();
            if( !beers.isEmpty() )
            {
                return outcomes().conflict().withBody( "Does not have zero beers." ).build();
            }
            em.remove( brewery );
            em.getTransaction().commit();
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
    {
        Representation resource = hal.
            newRepresentation( reverseRoutes().of( GET( API.class ).beers() ).httpUrl() ).
            withLink( "api", reverseRoutes().of( GET( API.class ).index() ).httpUrl() );
        EntityManager em = emf.createEntityManager();
        try
        {
            List<Beer> beers = em.createQuery( "select b from Beer b", Beer.class ).getResultList();
            resource.withProperty( "count", beers.size() );
            for( Beer beer : beers )
            {
                Representation beerResource = hal.
                    newRepresentation( reverseRoutes().of( GET( API.class ).beer( beer.getId() ) ).httpUrl() ).
                    withProperty( "id", beer.getId() ).
                    withProperty( "name", beer.getName() ).
                    withProperty( "brewery_id", beer.getBrewery().getId() ).
                    withProperty( "brewery_name", beer.getBrewery().getName() );
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
        if( !APPLICATION_JSON.equals( request().contentType() ) )
        {
            return outcomes().badRequest().
                withBody( "Unacceptable content-type:" + request().contentType() ).build();
        }
        String body = request().body().asString();
        Long breweryId;
        String name, description;
        Float abv;
        try
        {
            ReadableRepresentation input = hal.readRepresentation( new StringReader( body ) );
            breweryId = Long.valueOf( input.getValue( "brewery_id" ).toString().trim() );
            name = input.getValue( "name" ).toString().trim();
            abv = Float.valueOf( input.getValue( "abv" ).toString().trim() );
            description = input.getValue( "description" ).toString().trim();
        }
        catch( RepresentationException | NumberFormatException ex )
        {
            return outcomes().badRequest().
                withBody( ex.getMessage() ).build();
        }
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            Brewery brewery = em.find( Brewery.class, breweryId );
            if( brewery == null )
            {
                return outcomes().badRequest().withBody( "No brewery found with id " + breweryId ).build();
            }
            Beer beer = Beer.newBeer( brewery, name, abv, description );
            em.persist( beer );
            em.getTransaction().commit();
            String beerRoute = reverseRoutes().of( GET( API.class ).beer( beer.getId() ) ).httpUrl();
            String json = hal.newRepresentation().
                withProperty( "id", beer.getId() ).
                withProperty( "url", beerRoute ).
                toString( HAL_JSON );
            return outcomes().
                created().
                withHeader( LOCATION, beerRoute ).
                as( APPLICATION_JSON ).
                withBody( json ).
                build();
        }
        catch( ConstraintViolationException ex )
        {
            return outcomes().badRequest().
                withBody( ex.getConstraintViolations().toString() ).build();
        }
        finally
        {
            em.close();
        }
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
                withLink( "api", reverseRoutes().of( GET( API.class ).index() ).httpUrl() ).
                withLink( "list", reverseRoutes().of( GET( API.class ).beers() ).httpUrl() ).
                withProperty( "id", beer.getId() ).
                withProperty( "name", beer.getName() ).
                withProperty( "abv", beer.getAbv() ).
                withProperty( "description", beer.getDescription() );
            Representation breweryResource = hal.
                newRepresentation( reverseRoutes().of( GET( API.class ).brewery( beer.getBrewery().getId() ) ).httpUrl() ).
                withProperty( "id", beer.getBrewery().getId() ).
                withProperty( "name", beer.getBrewery().getName() );
            resource.withRepresentation( "brewery", breweryResource );
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
        if( !APPLICATION_JSON.equals( request().contentType() ) )
        {
            return outcomes().badRequest().
                withBody( "Unacceptable content-type:" + request().contentType() ).build();
        }
        String body = request().body().asString();
        String name, description;
        Float abv;
        try
        {
            ReadableRepresentation input = hal.readRepresentation( new StringReader( body ) );
            name = input.getValue( "name" ).toString().trim();
            abv = Float.valueOf( input.getValue( "abv" ).toString().trim() );
            description = input.getValue( "description" ).toString().trim();
        }
        catch( RepresentationException ex )
        {
            return outcomes().badRequest().
                withBody( ex.getMessage() ).build();
        }
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            Beer beer = em.find( Beer.class, id );
            if( beer == null )
            {
                return outcomes().notFound().build();
            }
            beer.setName( name );
            beer.setAbv( abv );
            beer.setDescription( description );
            em.persist( beer );
            em.getTransaction().commit();
            return outcomes().ok().build();
        }
        catch( ConstraintViolationException ex )
        {
            return outcomes().badRequest().
                withBody( ex.getConstraintViolations().toString() ).build();
        }
        finally
        {
            em.close();
        }
    }

    public Outcome deleteBeer( Long id )
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            Beer beer = em.find( Beer.class, id );
            if( beer == null )
            {
                return outcomes().notFound().build();
            }
            em.remove( beer );
            em.getTransaction().commit();
            return outcomes().ok().build();
        }
        finally
        {
            em.close();
        }
    }
}
