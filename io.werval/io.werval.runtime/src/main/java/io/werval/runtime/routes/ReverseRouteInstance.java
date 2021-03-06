/*
 * Copyright (c) 2013-2014 the original author or authors
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
package io.werval.runtime.routes;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.werval.api.context.CurrentContext;
import io.werval.api.http.Method;
import io.werval.api.http.QueryString;
import io.werval.api.routes.ReverseRoute;
import io.werval.runtime.http.HttpConstants;
import io.werval.runtime.util.Comparators;
import io.werval.util.IllegalArguments;
import io.werval.util.Strings;

/**
 * Reverse route instance.
 */
public class ReverseRouteInstance
    implements ReverseRoute
{
    private final Method method;
    private final String uri;
    private final Charset charset;
    private final Map<String, List<String>> appendedQueryString = new TreeMap<>( Comparators.LOWER_CASE );
    private String fragmentIdentifier;

    public ReverseRouteInstance( Method method, String uri, Charset charset )
    {
        this.method = method;
        this.uri = uri;
        this.charset = charset;
    }

    @Override
    public Method method()
    {
        return method;
    }

    @Override
    public String uri()
    {
        QueryString.Encoder encoder = new QueryString.Encoder( uri, charset );
        for( Map.Entry<String, List<String>> entry : appendedQueryString.entrySet() )
        {
            String key = entry.getKey();
            for( String value : entry.getValue() )
            {
                encoder.addParam( key, value );
            }
        }
        StringBuilder sb = new StringBuilder( encoder.toString() );
        if( !Strings.isEmpty( fragmentIdentifier ) )
        {
            sb.append( "#" ).append( fragmentIdentifier );
        }
        return sb.toString();
    }

    @Override
    public ReverseRoute appendQueryString( String key, String... values )
    {
        IllegalArguments.ensureNotEmpty( "key", key );
        IllegalArguments.ensureNotEmpty( "values", values );
        if( !appendedQueryString.containsKey( key ) )
        {
            appendedQueryString.put( key, new ArrayList<>() );
        }
        appendedQueryString.get( key ).addAll( Arrays.asList( values ) );
        return this;
    }

    @Override
    public ReverseRoute appendQueryString( Map<String, ?> parameters )
    {
        IllegalArguments.ensureNotNull( "parameters", parameters );
        for( Map.Entry<String, ?> entry : parameters.entrySet() )
        {
            String key = entry.getKey();
            IllegalArguments.ensureNotEmpty( "parameter key", key );
            IllegalArguments.ensureNotNull( "parameter value for '" + key + "'", entry.getValue() );
            if( !appendedQueryString.containsKey( key ) )
            {
                appendedQueryString.put( key, new ArrayList<>() );
            }
            if( entry.getValue() instanceof List )
            {
                for( Object value : (List) entry.getValue() )
                {
                    appendedQueryString.get( key ).add( value.toString() );
                }
            }
            else
            {
                appendedQueryString.get( key ).add( entry.getValue().toString() );
            }
        }
        return this;
    }

    @Override
    public ReverseRoute withFragmentIdentifier( String fragmentIdentifier )
    {
        this.fragmentIdentifier = fragmentIdentifier;
        return this;
    }

    @Override
    public String httpUrl()
    {
        return httpUrl( false );
    }

    @Override
    public String httpUrl( boolean secure )
    {
        return absoluteUrl( "http", secure );
    }

    @Override
    public String webSocketUrl()
    {
        return webSocketUrl( false );
    }

    @Override
    public String webSocketUrl( boolean secure )
    {
        return absoluteUrl( "ws", secure );
    }

    private String absoluteUrl( String protocol, boolean secure )
    {
        StringBuilder absoluteUrl = new StringBuilder( protocol );
        if( secure )
        {
            absoluteUrl.append( "s" );
        }
        absoluteUrl.append( "://" ).append( CurrentContext.request().domain() );
        if( ( !secure && CurrentContext.request().port() != HttpConstants.DEFAULT_HTTP_PORT )
            || ( secure && CurrentContext.request().port() != HttpConstants.DEFAULT_HTTPS_PORT ) )
        {
            // With custom port
            absoluteUrl.append( ":" ).append( CurrentContext.request().port() );
        }
        absoluteUrl.append( uri() );
        return absoluteUrl.toString();
    }

    @Override
    public String toString()
    {
        return uri();
    }
}
