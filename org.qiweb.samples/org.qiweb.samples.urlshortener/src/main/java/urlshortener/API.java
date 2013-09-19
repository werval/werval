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
import java.net.URL;
import java.util.Collection;
import org.qiweb.api.controllers.Outcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qiweb.api.controllers.Controller.outcomes;
import static org.qiweb.api.mime.MimeTypes.APPLICATION_JSON;

/**
 * URL Shortener HTTP API.
 */
public class API
{

    private static final Logger LOG = LoggerFactory.getLogger( API.class );
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    /**
     * List shortened urls and their hash.
     *
     * @return  application/json array filled with Link objects.
     */
    public Outcome list()
        throws JsonProcessingException
    {
        Collection<Link> list = ShortenerService.INSTANCE.list();
        String json = JSON_MAPPER.writeValueAsString( list );
        LOG.info( "List is {}", json );
        return outcomes().ok( json ).as( APPLICATION_JSON ).build();
    }

    /**
     * Shorten a URL.
     *
     * @param url Long URL
     * @return application/json Link object.
     */
    public Outcome shorten( URL url )
        throws JsonProcessingException
    {
        Link link = ShortenerService.INSTANCE.shorten( url.toString().trim() );
        String json = JSON_MAPPER.writeValueAsString( link );
        LOG.info( "Shorten {} to {} and 200 {}", link.longUrl, link.shortUrl(), json );
        return outcomes().ok( json ).as( APPLICATION_JSON ).build();
    }

    /**
     * Expand a hash to its corresponding long URL.
     *
     * @param hash Hash
     * @return application/json Link object.
     */
    public Outcome expand( String hash )
        throws JsonProcessingException
    {
        Link link = ShortenerService.INSTANCE.link( hash.trim() );
        if( link == null )
        {
            LOG.info( "Expand fail with 404 for {}", hash.trim() );
            return outcomes().notFound().as( APPLICATION_JSON ).build();
        }
        String json = JSON_MAPPER.writeValueAsString( link );
        LOG.info( "Expand {} to {} and 200 {}", link.hash, link.longUrl, json );
        return outcomes().ok( json ).as( APPLICATION_JSON ).build();
    }

    /**
     * Lookup existing shortened URLs from long URL.
     *
     * @param url Long URL
     * @return application/json array filled with Link object.
     */
    public Outcome lookup( URL url )
        throws JsonProcessingException
    {
        String urlString = url.toString().trim();
        Collection<Link> list = ShortenerService.INSTANCE.lookup( urlString );
        if( list.isEmpty() )
        {
            LOG.info( "Lookup fail with 404 for {}", urlString );
            return outcomes().notFound().as( APPLICATION_JSON ).build();
        }
        String json = JSON_MAPPER.writeValueAsString( list );
        LOG.info( "Lookup {} found {} link(s) and 200 {}", urlString, list.size(), json );
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
        Link link = ShortenerService.INSTANCE.link( hash.trim() );
        if( link == null )
        {
            LOG.info( "Redirect fail with 404 for {}", hash.trim() );
            return outcomes().notFound().as( APPLICATION_JSON ).build();
        }
        LOG.info( "Redirect {} to 303 {}", link.hash, link.longUrl );
        link.clicks += 1;
        return outcomes().seeOther( link.longUrl ).build();
    }
}
