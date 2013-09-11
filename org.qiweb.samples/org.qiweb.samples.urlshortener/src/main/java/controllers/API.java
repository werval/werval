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
package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.Map;
import org.qiweb.api.controllers.Outcome;

import static org.qiweb.api.controllers.Controller.outcomes;
import static org.qiweb.api.controllers.Controller.reverseRoutes;
import static org.qiweb.api.mime.MimeTypes.APPLICATION_JSON;
import static org.qiweb.api.routes.ReverseRoutes.GET;

public class API
{

    private static final JsonNodeFactory JSON_FACTORY = JsonNodeFactory.instance;
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    public Outcome list()
        throws JsonProcessingException
    {
        Map<String, String> list = Shortener.INSTANCE.list();
        String json = JSON_MAPPER.writeValueAsString( list );
        return outcomes().ok( json ).as( APPLICATION_JSON ).build();
    }

    public Outcome shorten( String longUrl )
        throws JsonProcessingException
    {
        String hash = Shortener.INSTANCE.shorten( longUrl );
        String shortUrl = reverseRoutes().of( GET( API.class ).redirect( hash ) ).httpUrl();
        String json = JSON_MAPPER.writeValueAsString( JSON_FACTORY.objectNode().
            put( "hash", hash ).
            put( "short_url", shortUrl ) );
        return outcomes().ok( json ).as( APPLICATION_JSON ).build();
    }

    public Outcome expand( String hash )
        throws JsonProcessingException
    {
        String longUrl = Shortener.INSTANCE.expand( hash );
        if( longUrl == null )
        {
            return outcomes().notFound().as( APPLICATION_JSON ).build();
        }
        String json = JSON_MAPPER.writeValueAsString( JSON_FACTORY.objectNode().
            put( "long_url", longUrl ) );
        return outcomes().ok( json ).as( APPLICATION_JSON ).build();
    }

    public Outcome lookup( String longUrl )
        throws JsonProcessingException
    {
        String hash = Shortener.INSTANCE.lookup( longUrl );
        if( hash == null )
        {
            return outcomes().notFound().as( APPLICATION_JSON ).build();
        }
        String shortUrl = reverseRoutes().of( GET( API.class ).redirect( hash ) ).httpUrl();
        String json = JSON_MAPPER.writeValueAsString( JSON_FACTORY.objectNode().
            put( "hash", hash ).
            put( "short_url", shortUrl ) );
        return outcomes().ok( json ).as( APPLICATION_JSON ).build();
    }

    public Outcome redirect( String hash )
    {
        String longUrl = Shortener.INSTANCE.expand( hash );
        if( longUrl == null )
        {
            return outcomes().notFound().as( APPLICATION_JSON ).build();
        }
        return outcomes().seeOther( longUrl ).build();
    }
}
