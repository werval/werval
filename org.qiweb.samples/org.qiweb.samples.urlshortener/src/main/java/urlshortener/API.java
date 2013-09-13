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
package urlshortener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.Collection;
import org.qiweb.api.controllers.Outcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qiweb.api.controllers.Controller.outcomes;
import static org.qiweb.api.controllers.Controller.reverseRoutes;
import static org.qiweb.api.mime.MimeTypes.APPLICATION_JSON;
import static org.qiweb.api.routes.ReverseRoutes.GET;

/**
 * URL Shortener HTTP API.
 */
public class API
{

    private static final Logger LOG = LoggerFactory.getLogger( API.class );
    private static final JsonNodeFactory JSON_FACTORY = JsonNodeFactory.instance;
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    /**
     * List shortened urls and their hash.
     *
     * @return  application/json array filled with <code>{ "hash": "ABCD", "long_url": "http://qiweb.org/" }</code>
     *          objects.
     */
    public Outcome list()
        throws JsonProcessingException
    {
        Collection<ShortenerService.Link> list = ShortenerService.INSTANCE.list();
        String json = JSON_MAPPER.writeValueAsString( list );
        LOG.info( "List is {}", json );
        return outcomes().ok( json ).as( APPLICATION_JSON ).build();
    }

    /**
     * Shorten a URL.
     *
     * @param longUrl Long URL
     * @return application/json object with <code>hash</code> and <code>short_url</code> keys.
     */
    public Outcome shorten( String longUrl )
        throws JsonProcessingException
    {
        String hash = ShortenerService.INSTANCE.shorten( longUrl );
        String shortUrl = reverseRoutes().of( GET( API.class ).redirect( hash ) ).httpUrl();
        String json = JSON_MAPPER.writeValueAsString( JSON_FACTORY.objectNode().
            put( "hash", hash ).
            put( "short_url", shortUrl ) );
        LOG.info( "Shorten {} to {} and 200 {}", longUrl, shortUrl, json );
        return outcomes().ok( json ).as( APPLICATION_JSON ).build();
    }

    /**
     * Expand a hash to its corrsponding long URL.
     *
     * @param hash Hash
     * @return application/json object with <code>hash</code> and <code>long_url</code> keys.
     */
    public Outcome expand( String hash )
        throws JsonProcessingException
    {
        String longUrl = ShortenerService.INSTANCE.expand( hash );
        if( longUrl == null )
        {
            LOG.info( "Expand fail with 404 for {}", hash );
            return outcomes().notFound().as( APPLICATION_JSON ).build();
        }
        String json = JSON_MAPPER.writeValueAsString( JSON_FACTORY.objectNode().
            put( "hash", hash ).
            put( "long_url", longUrl ) );
        LOG.info( "Expand {} to {} and 200 {}", hash, longUrl, json );
        return outcomes().ok( json ).as( APPLICATION_JSON ).build();
    }

    /**
     * Lookup existing shortened URL from long URL.
     *
     * @param longUrl Long URL
     * @return application/json object with <code>hash</code> and <code>short_url</code> keys.
     */
    public Outcome lookup( String longUrl )
        throws JsonProcessingException
    {
        String hash = ShortenerService.INSTANCE.lookup( longUrl );
        if( hash == null )
        {
            LOG.info( "Lookup fail with 404 for {}", longUrl );
            return outcomes().notFound().as( APPLICATION_JSON ).build();
        }
        String shortUrl = reverseRoutes().of( GET( API.class ).redirect( hash ) ).httpUrl();
        String json = JSON_MAPPER.writeValueAsString( JSON_FACTORY.objectNode().
            put( "hash", hash ).
            put( "short_url", shortUrl ) );
        LOG.info( "Lookup {} found {} and 200 {}", longUrl, shortUrl, json );
        return outcomes().ok( json ).as( APPLICATION_JSON ).build();
    }

    /**
     * Redirect to long URL from hash.
     *
     * @param hash Hash
     * @return 303 Redirection to long URL
     */
    public Outcome redirect( String hash )
    {
        String longUrl = ShortenerService.INSTANCE.expand( hash );
        if( longUrl == null )
        {
            LOG.info( "Redirect fail with 404 for {}", hash );
            return outcomes().notFound().as( APPLICATION_JSON ).build();
        }
        LOG.info( "Redirect {} to 303 {}", hash, longUrl );
        return outcomes().seeOther( longUrl ).build();
    }
}
