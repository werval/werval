/*
 * Copyright (c) 2014 the original author or authors
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
package org.qiweb.modules.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.io.UncheckedIOException;

import static org.qiweb.api.exceptions.IllegalArguments.ensureNotEmpty;
import static org.qiweb.api.util.Charsets.UTF_8;

/**
 * Jackson JSON.
 */
public class JacksonJSON
    implements JSON
{
    private final ObjectMapper mapper;

    public JacksonJSON( ObjectMapper mapper )
    {
        this.mapper = mapper;
    }

    @Override
    public ObjectMapper mapper()
    {
        return mapper;
    }

    @Override
    public byte[] toJSON( Object object )
    {
        try
        {
            return mapper.writeValueAsBytes( object );
        }
        catch( JsonProcessingException ex )
        {
            throw new JsonPluginException( ex );
        }
    }

    @Override
    public byte[] toJSON( Object object, Class<?> jsonView )
    {
        try
        {
            return mapper.writerWithView( jsonView ).writeValueAsBytes( object );
        }
        catch( JsonProcessingException ex )
        {
            throw new JsonPluginException( ex );
        }
    }

    @Override
    public String toJSONString( Object object )
    {
        return new String( toJSON( object ), UTF_8 );
    }

    @Override
    public String toJSONString( Object object, Class<?> jsonView )
    {
        return new String( toJSON( object, jsonView ), UTF_8 );
    }

    @Override
    public JsonNode toNode( Object object )
    {
        try
        {
            return mapper.readTree( mapper.writer().writeValueAsBytes( object ) );
        }
        catch( JsonProcessingException ex )
        {
            throw new JsonPluginException( ex );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }

    @Override
    public JsonNode toNode( Object object, Class<?> jsonView )
    {
        try
        {
            return mapper.readTree( mapper.writerWithView( jsonView ).writeValueAsBytes( object ) );
        }
        catch( JsonProcessingException ex )
        {
            throw new JsonPluginException( ex );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }

    @Override
    public JsonNode fromJSON( byte[] json )
    {
        try
        {
            return mapper.readTree( json );
        }
        catch( JsonProcessingException ex )
        {
            throw new JsonPluginException( ex );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }

    @Override
    public JsonNode fromJSON( String json )
    {
        return fromJSON( json.getBytes( UTF_8 ) );
    }

    @Override
    public <T> T fromJSON( Class<T> type, byte[] json )
    {
        try
        {
            return mapper.readValue( json, type );
        }
        catch( JsonProcessingException ex )
        {
            throw new JsonPluginException( ex );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }

    @Override
    public <T> T fromJSON( Class<T> type, String json )
    {
        return fromJSON( type, json.getBytes( UTF_8 ) );
    }

    @Override
    public <T> T fromNode( Class<T> type, JsonNode node )
    {
        try
        {
            return mapper.treeToValue( node, type );
        }
        catch( JsonProcessingException ex )
        {
            throw new JsonPluginException( ex );
        }
    }

    @Override
    public <T> T updateFromJSON( T object, byte[] json )
    {
        try
        {
            return mapper.readerForUpdating( object ).readValue( json );
        }
        catch( JsonProcessingException ex )
        {
            throw new JsonPluginException( ex );
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }

    @Override
    public <T> T updateFromJSON( T object, String json )
    {
        return updateFromJSON( object, json.getBytes( UTF_8 ) );
    }

    @Override
    public ObjectNode newObject()
    {
        return mapper.createObjectNode();
    }

    @Override
    public ArrayNode newArray()
    {
        return mapper.createArrayNode();
    }

    @Override
    public byte[] toJSONP( Object object )
    {
        return toJSONPString( object ).getBytes( UTF_8 );
    }

    @Override
    public byte[] toJSONP( Object object, Class<?> jsonView )
    {
        return toJSONPString( object, jsonView ).getBytes( UTF_8 );
    }

    @Override
    public byte[] toJSONP( String callbackFunctionName, Object object )
    {
        return toJSONPString( callbackFunctionName, object ).getBytes( UTF_8 );
    }

    @Override
    public byte[] toJSONP( String callbackFunctionName, Object object, Class<?> jsonView )
    {
        return toJSONPString( callbackFunctionName, object, jsonView ).getBytes( UTF_8 );
    }

    @Override
    public String toJSONPString( Object object )
    {
        return toJSONPString( DEFAULT_JSONP_CALLBACK, object );
    }

    @Override
    public String toJSONPString( String callbackFunctionName, Object object )
    {
        return wrapJsonP( callbackFunctionName, toJSONString( object ) );
    }

    @Override
    public String toJSONPString( Object object, Class<?> jsonView )
    {
        return toJSONPString( DEFAULT_JSONP_CALLBACK, object, jsonView );
    }

    @Override
    public String toJSONPString( String callbackFunctionName, Object object, Class<?> jsonView )
    {
        return wrapJsonP( callbackFunctionName, toJSONString( object, jsonView ) );
    }

    private static String wrapJsonP( String callbackFunctionName, String json )
    {
        ensureNotEmpty( "Callback function name", callbackFunctionName );
        return callbackFunctionName + "(" + json + ");";
    }
}
