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
import org.qiweb.api.outcomes.Outcome;

import static org.qiweb.api.context.CurrentContext.application;
import static org.qiweb.api.context.CurrentContext.outcomes;
import static org.qiweb.api.mime.MimeTypesNames.APPLICATION_JSON;

/**
 * URL Shortener HTTP API.
 */
public class API
{

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final ShortenerService shortener;
    private final String jsonMime;

    public API()
    {
        this.shortener = application().metaData().get( ShortenerService.class, "shortener" );
        this.jsonMime = application().mimeTypes().withCharsetOfTextual( APPLICATION_JSON );
    }

    /**
     * List shortened urls and their hash.
     *
     * @return  application/json array filled with Link objects.
     * @throws JsonProcessingException
     */
    public Outcome list()
        throws JsonProcessingException
    {
        Collection<Link> list = shortener.list();
        byte[] json = MAPPER.writeValueAsBytes( list );
        return outcomes().ok( json ).as( jsonMime ).build();
    }

    /**
     * Shorten a URL.
     *
     * @param url Long URL
     * @return application/json Link object.
     * @throws JsonProcessingException
     */
    public Outcome shorten( URL url )
        throws JsonProcessingException
    {
        Link link = shortener.shorten( url.toString().trim() );
        String json = MAPPER.writeValueAsString( link );
        return outcomes().ok( json ).as( jsonMime ).build();
    }

    /**
     * Expand a hash to its corresponding long URL.
     *
     * @param hash Hash
     * @return application/json Link object.
     * @throws JsonProcessingException
     */
    public Outcome expand( String hash )
        throws JsonProcessingException
    {
        Link link = shortener.link( hash.trim() );
        if( link == null )
        {
            return outcomes().notFound().build();
        }
        byte[] json = MAPPER.writeValueAsBytes( link );
        return outcomes().ok( json ).as( jsonMime ).build();
    }

    /**
     * Lookup existing shortened URLs from long URL.
     *
     * @param url Long URL
     * @return application/json array filled with Link object.
     * @throws JsonProcessingException
     */
    public Outcome lookup( URL url )
        throws JsonProcessingException
    {
        String urlString = url.toString().trim();
        Collection<Link> list = shortener.lookup( urlString );
        if( list.isEmpty() )
        {
            return outcomes().notFound().build();
        }
        byte[] json = MAPPER.writeValueAsBytes( list );
        return outcomes().ok( json ).as( jsonMime ).build();
    }

    /**
     * Redirect to long URL from hash.
     *
     * @param hash Hash
     * @return 303 Redirection to long URL
     */
    public Outcome redirect( String hash )
    {
        Link link = shortener.link( hash.trim() );
        if( link == null )
        {
            return outcomes().notFound().build();
        }
        link.clicks += 1;
        return outcomes().seeOther( link.longUrl ).build();
    }

}
