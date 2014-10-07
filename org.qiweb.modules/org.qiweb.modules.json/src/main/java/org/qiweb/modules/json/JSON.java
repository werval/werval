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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.qiweb.api.context.CurrentContext;

/**
 * JSON API.
 */
public interface JSON
{
    /**
     * Current JSON Plugin API.
     *
     * @return Current JSON Plugin API
     *
     * @throws IllegalArgumentException if no {@literal JSON Plugin} is found
     * @throws IllegalStateException    if the {@literal Application} is not active
     */
    static JSON json()
    {
        return CurrentContext.plugin( JSON.class );
    }

    /**
     * Name of the default javascript callback function to use with JSON-P.
     */
    String DEFAULT_JSONP_CALLBACK = "callback";

    /**
     * @return Jackson ObjectMapper
     */
    ObjectMapper mapper();

    /**
     * Create JSON for an Object.
     *
     * @param object Object to map to JSON
     *
     * @return JSON bytes
     */
    byte[] toJSON( Object object );

    /**
     * Create JSON for an Object for a specific {@literal JSON View}.
     *
     * See <a href="http://wiki.fasterxml.com/JacksonJsonViews">JacksonJsonViews</a>.
     *
     * @param object   Object to map to JSON
     * @param jsonView JSON View to use
     *
     * @return JSON bytes
     */
    byte[] toJSON( Object object, Class<?> jsonView );

    /**
     * Create JSON for an Object.
     *
     * @param object Object to map to JSON
     *
     * @return JSON as String
     */
    String toJSONString( Object object );

    /**
     * Create JSON for an Object for a specific {@literal JSON View}.
     *
     * See <a href="http://wiki.fasterxml.com/JacksonJsonViews">JacksonJsonViews</a>.
     *
     * @param object   Object to map to JSON
     * @param jsonView JSON View to use
     *
     * @return JSON as String
     */
    String toJSONString( Object object, Class<?> jsonView );

    /**
     * Create {@literal JsonNode} for an Object.
     *
     * @param object Object to map to JsonNode
     *
     * @return JSON as {@literal JsonNode}
     */
    JsonNode toNode( Object object );

    /**
     * Create {@literal JsonNode} for an Object for a specific {@literal JSON View}.
     *
     * See <a href="http://wiki.fasterxml.com/JacksonJsonViews">JacksonJsonViews</a>.
     *
     * @param object   Object to map to JSON
     * @param jsonView JSON View to use
     *
     * @return JSON as {@literal JsonNode}
     */
    JsonNode toNode( Object object, Class<?> jsonView );

    /**
     * Parse a {@literal JsonNode} out of bytes.
     *
     * @param json JSON bytes
     *
     * @return Parsed {@literal JsonNode}
     */
    JsonNode fromJSON( byte[] json );

    /**
     * Parse a {@literal JsonNode} out of a String.
     *
     * @param json JSON bytes
     *
     * @return Parsed {@literal JsonNode}
     */
    JsonNode fromJSON( String json );

    /**
     * Create an Object of a given type from JSON bytes.
     *
     * @param <T>  Parameterized type of the Object
     * @param type Type of the Object
     * @param json JSON bytes
     *
     * @return Created Object of the given type
     */
    <T> T fromJSON( Class<T> type, byte[] json );

    /**
     * Create an Object of a given type from a JSON String.
     *
     * @param <T>  Parameterized type of the Object
     * @param type Type of the Object
     * @param json JSON as String
     *
     * @return Created Object of the given type
     */
    <T> T fromJSON( Class<T> type, String json );

    /**
     * Create an Object of a given type from a {@literal JsonNode}.
     *
     * @param <T>  Parameterized type of the Object
     * @param type Type of the Object
     * @param node {@literal JsonNode}
     *
     * @return Created Object of the given type
     */
    <T> T fromNode( Class<T> type, JsonNode node );

    /**
     * Update an Object with data from JSON bytes.
     *
     * @param <T>    Parameterized type of the Object
     * @param object The Object to update
     * @param json   JSON bytes
     *
     * @return The given object eventually updated
     */
    <T> T updateFromJSON( T object, byte[] json );

    /**
     * Update an Object with data from a JSON String.
     *
     * @param <T>    Parameterized type of the Object
     * @param object The Object to update
     * @param json   JSON as String
     *
     * @return The given object eventually updated
     */
    <T> T updateFromJSON( T object, String json );

    /**
     * Create a new {@literal ObjectNode}.
     *
     * @return A new {@literal ObjectNode}
     */
    ObjectNode newObject();

    /**
     * Create a new {@literal ArrayNode}.
     *
     * @return A new {@literal ArrayNode}
     */
    ArrayNode newArray();

    /**
     * Create a JSON-P payload for an Object.
     *
     * See <a href="http://en.wikipedia.org/wiki/JSONP">JSONP</a>.
     *
     * @param object Object to map to JSON-P
     *
     * @return JSON-P bytes
     */
    byte[] toJSONP( Object object );

    /**
     * Create a JSON-P payload for an Object for a given callback function.
     *
     * See <a href="http://en.wikipedia.org/wiki/JSONP">JSONP</a>.
     *
     * @param callbackFunctionName Name of the JSON-P callback function
     * @param object               Object to map to JSON-P
     *
     * @return JSON-P bytes
     */
    byte[] toJSONP( String callbackFunctionName, Object object );

    /**
     * Create a JSON-P payload for an Object for a specific {@literal JSON View}.
     *
     * See <a href="http://en.wikipedia.org/wiki/JSONP">JSONP</a> and
     * <a href="http://wiki.fasterxml.com/JacksonJsonViews">JacksonJsonViews</a>.
     *
     * @param object   Object to map to JSON-P
     * @param jsonView JSON View to use
     *
     * @return JSON-P bytes
     */
    byte[] toJSONP( Object object, Class<?> jsonView );

    /**
     * Create a JSON-P payload for an Object for a given callback function and for a specific {@literal JSON View}.
     *
     * See <a href="http://en.wikipedia.org/wiki/JSONP">JSONP</a> and
     * <a href="http://wiki.fasterxml.com/JacksonJsonViews">JacksonJsonViews</a>.
     *
     * @param callbackFunctionName Name of the JSON-P callback function
     * @param object               Object to map to JSON-P
     * @param jsonView
     *
     * @return JSON-P bytes
     */
    byte[] toJSONP( String callbackFunctionName, Object object, Class<?> jsonView );

    /**
     * Create a JSON-P String for an Object.
     *
     * See <a href="http://en.wikipedia.org/wiki/JSONP">JSONP</a>.
     *
     * @param object Object to map to JSON-P
     *
     * @return JSON-P as String
     */
    String toJSONPString( Object object );

    /**
     * Create a JSON-P String for an Object for a given callback function.
     *
     * See <a href="http://en.wikipedia.org/wiki/JSONP">JSONP</a>.
     *
     * @param callbackFunctionName Name of the JSON-P callback function
     * @param object               Object to map to JSON-P
     *
     * @return JSON-P as String
     */
    String toJSONPString( String callbackFunctionName, Object object );

    /**
     * Create a JSON-P String for an Object for a specific {@literal JSON View}.
     *
     * See <a href="http://en.wikipedia.org/wiki/JSONP">JSONP</a> and
     * <a href="http://wiki.fasterxml.com/JacksonJsonViews">JacksonJsonViews</a>.
     *
     * @param object   Object to map to JSON-P
     * @param jsonView JSON View to use
     *
     * @return JSON-P as String
     */
    String toJSONPString( Object object, Class<?> jsonView );

    /**
     * Create a JSON-P String for an Object for a given callback function and for a specific {@literal JSON View}.
     *
     * See <a href="http://en.wikipedia.org/wiki/JSONP">JSONP</a> and
     * <a href="http://wiki.fasterxml.com/JacksonJsonViews">JacksonJsonViews</a>.
     *
     * @param callbackFunctionName Name of the JSON-P callback function
     * @param object               Object to map to JSON-P
     * @param jsonView
     *
     * @return JSON-P as String
     */
    String toJSONPString( String callbackFunctionName, Object object, Class<?> jsonView );
}
