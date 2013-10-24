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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.validation.ConstraintViolationException;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.samples.beerdb.BuildVersion;

import static org.qiweb.api.context.CurrentContext.application;
import static org.qiweb.api.context.CurrentContext.outcomes;
import static org.qiweb.api.context.CurrentContext.request;
import static org.qiweb.api.context.CurrentContext.reverseRoutes;
import static org.qiweb.api.http.Headers.Names.LOCATION;
import static org.qiweb.api.mime.MimeTypesNames.APPLICATION_JSON;
import static org.qiweb.api.routes.ReverseRoutes.GET;

public class API
{

    private final EntityManagerFactory emf;
    private final ObjectMapper mapper;

    public API()
    {
        this.emf = application().metaData().get( EntityManagerFactory.class, "emf" );
        this.mapper = application().metaData().get( ObjectMapper.class, "mapper" );
    }

    public Outcome index()
        throws JsonProcessingException
    {
        byte[] json = mapper.writeValueAsBytes( mapper.createObjectNode().
            put( "commit", BuildVersion.COMMIT ).
            put( "date", BuildVersion.DATE ).
            put( "detailed_version", BuildVersion.DETAILED_VERSION ).
            put( "dirty", BuildVersion.DIRTY ).
            put( "name", BuildVersion.NAME ).
            put( "version", BuildVersion.VERSION ) );
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
        EntityManager em = emf.createEntityManager();
        try
        {
            List<Brewery> breweries = em.createQuery( "select b from Brewery b", Brewery.class ).getResultList();
            byte[] json = mapper.writerWithView( Json.BreweryListView.class ).writeValueAsBytes( breweries );
            return outcomes().ok( json ).as( APPLICATION_JSON ).build();
        }
        finally
        {
            em.close();
        }
    }

    public Outcome createBrewery()
        throws IOException
    {
        if( !APPLICATION_JSON.equals( request().contentType() ) )
        {
            return badRequest( "Unacceptable content-type:" + request().contentType() );
        }
        byte[] body = request().body().asBytes();
        Brewery brewery;
        try
        {
            brewery = mapper.readValue( body, Brewery.class );
        }
        catch( JsonProcessingException ex )
        {
            return badRequest( ex.getMessage() );
        }
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            em.persist( brewery );
            em.getTransaction().commit();

            String breweryRoute = reverseRoutes().of( GET( API.class ).brewery( brewery.getId() ) ).httpUrl();
            byte[] json = mapper.writerWithView( Json.BreweryListView.class ).writeValueAsBytes( brewery );
            return outcomes().
                created().
                withHeader( LOCATION, breweryRoute ).
                as( APPLICATION_JSON ).
                withBody( json ).
                build();
        }
        catch( ConstraintViolationException ex )
        {
            return badRequest( ex.getConstraintViolations().toString() );
        }
        finally
        {
            em.close();
        }
    }

    public Outcome brewery( Long id )
        throws JsonProcessingException
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            Brewery brewery = em.find( Brewery.class, id );
            if( brewery == null )
            {
                return notFound( "Brewery" );
            }
            byte[] json = mapper.writerWithView( Json.BreweryDetailView.class ).writeValueAsBytes( brewery );
            return outcomes().ok( json ).as( APPLICATION_JSON ).build();
        }
        finally
        {
            em.close();
        }
    }

    public Outcome updateBrewery( Long id )
        throws IOException
    {
        if( !APPLICATION_JSON.equals( request().contentType() ) )
        {
            return badRequest( "Unacceptable content-type:" + request().contentType() );
        }
        byte[] body = request().body().asBytes();
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            Brewery brewery = em.find( Brewery.class, id );
            if( brewery == null )
            {
                return notFound( "Brewery" );
            }
            mapper.readerForUpdating( brewery ).readValue( body );
            em.persist( brewery );
            em.getTransaction().commit();
            return outcomes().ok().build();
        }
        catch( ConstraintViolationException ex )
        {
            return badRequest( ex.getConstraintViolations().toString() );
        }
        finally
        {
            em.close();
        }
    }

    public Outcome deleteBrewery( Long id )
        throws JsonProcessingException
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            Brewery brewery = em.find( Brewery.class, id );
            if( brewery == null )
            {
                return notFound( "Brewery" );
            }
            if( brewery.getBeersCount() > 0 )
            {
                return outcomes().conflict().withBody( "Does not have zero beers." ).as( APPLICATION_JSON ).build();
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
        throws JsonProcessingException
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            List<Beer> beers = em.createQuery( "select b from Beer b", Beer.class ).getResultList();
            byte[] json = mapper.writerWithView( Json.BeerListView.class ).writeValueAsBytes( beers );
            return outcomes().ok( json ).as( APPLICATION_JSON ).build();
        }
        finally
        {
            em.close();
        }
    }

    public Outcome createBeer()
        throws IOException
    {
        if( !APPLICATION_JSON.equals( request().contentType() ) )
        {
            return badRequest( "Unacceptable content-type:" + request().contentType() );
        }
        byte[] body = request().body().asBytes();
        JsonNode bodyNode = mapper.readTree( body );
        if( !bodyNode.hasNonNull( "brewery_id" ) )
        {
            return badRequest( "Missing brewery_id" );
        }
        Long breweryId = bodyNode.get( "brewery_id" ).longValue();
        Beer beer;
        try
        {
            beer = mapper.treeToValue( bodyNode, Beer.class );
        }
        catch( JsonProcessingException ex )
        {
            return badRequest( ex.getMessage() );
        }
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            Brewery brewery = em.find( Brewery.class, breweryId );
            if( brewery == null )
            {
                return badRequest( "No brewery found with id " + breweryId );
            }
            brewery.addBeer( beer );
            em.persist( beer );
            em.persist( brewery );
            em.getTransaction().commit();
            String beerRoute = reverseRoutes().of( GET( API.class ).beer( beer.getId() ) ).httpUrl();
            byte[] json = mapper.writerWithView( Json.BeerListView.class ).writeValueAsBytes( beer );
            return outcomes().
                created().
                withHeader( LOCATION, beerRoute ).
                as( APPLICATION_JSON ).
                withBody( json ).
                build();
        }
        catch( ConstraintViolationException ex )
        {
            return badRequest( ex.getConstraintViolations().toString() );
        }
        finally
        {
            em.close();
        }
    }

    public Outcome beer( Long id )
        throws JsonProcessingException
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            Beer beer = em.find( Beer.class, id );
            if( beer == null )
            {
                return notFound( "Beer" );
            }
            byte[] json = mapper.writerWithView( Json.BeerDetailView.class ).writeValueAsBytes( beer );
            return outcomes().ok( json ).as( APPLICATION_JSON ).build();
        }
        finally
        {
            em.close();
        }
    }

    public Outcome updateBeer( Long id )
        throws IOException
    {
        if( !APPLICATION_JSON.equals( request().contentType() ) )
        {
            return badRequest( "Unacceptable content-type:" + request().contentType() );
        }
        byte[] body = request().body().asBytes();
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            Beer beer = em.find( Beer.class, id );
            if( beer == null )
            {
                return notFound( "Beer" );
            }
            mapper.readerForUpdating( beer ).readValue( body );
            em.persist( beer );
            em.getTransaction().commit();
            return outcomes().ok().build();
        }
        catch( ConstraintViolationException ex )
        {
            return badRequest( ex.getConstraintViolations().toString() );
        }
        finally
        {
            em.close();
        }
    }

    public Outcome deleteBeer( Long id )
        throws JsonProcessingException
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            Beer beer = em.find( Beer.class, id );
            if( beer == null )
            {
                return notFound( "Beer" );
            }
            Brewery brewery = beer.getBrewery();
            brewery.removeBeer( beer );
            em.remove( beer );
            em.persist( brewery );
            em.getTransaction().commit();
            return outcomes().ok().build();
        }
        finally
        {
            em.close();
        }
    }

    private Outcome notFound( String what )
        throws JsonProcessingException
    {
        return outcomes().notFound().withBody( errorBody( what + " not found" ) ).as( APPLICATION_JSON ).build();
    }

    private Outcome badRequest( String... messages )
        throws JsonProcessingException
    {
        return outcomes().badRequest().withBody( errorBody( messages ) ).as( APPLICATION_JSON ).build();
    }

    private byte[] errorBody( String... messages )
        throws JsonProcessingException
    {
        ObjectNode root = mapper.createObjectNode();
        ArrayNode errors = root.putArray( "errors" );
        for( String message : messages )
        {
            errors.add( message );
        }
        return mapper.writeValueAsBytes( root );
    }

}
