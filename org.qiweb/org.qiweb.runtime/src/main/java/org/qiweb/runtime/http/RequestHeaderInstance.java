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
package org.qiweb.runtime.http;

import java.util.HashMap;
import java.util.Map;
import org.qiweb.api.http.Cookies;
import org.qiweb.api.http.Headers;
import org.qiweb.api.http.QueryString;
import org.qiweb.api.http.RequestHeader;

import static java.util.Locale.US;
import static org.qiweb.api.http.Headers.Names.CONTENT_TYPE;
import static org.qiweb.api.http.Headers.Names.HOST;
import static org.qiweb.api.util.Strings.EMPTY;
import static org.qiweb.api.util.Strings.isEmpty;
import static org.qiweb.runtime.http.HttpConstants.DEFAULT_HTTPS_PORT;
import static org.qiweb.runtime.http.HttpConstants.DEFAULT_HTTP_PORT;

public class RequestHeaderInstance
    implements RequestHeader
{

    private static interface Lazy<T>
    {

        T get()
            throws IllegalStateException;

    }

    public static String extractCharset( String contentType )
    {
        if( isEmpty( contentType ) )
        {
            return EMPTY;
        }
        String[] split = contentType.split( ";" );
        if( split.length <= 1 )
        {
            return EMPTY;
        }
        for( int idx = 1; idx < split.length; idx++ )
        {
            String option = split[idx].trim().toLowerCase( US );
            if( option.startsWith( "charset" ) )
            {
                return option.split( "=" )[1];
            }
        }
        return EMPTY;
    }

    private final String identity;
    private final String version;
    private final String method;
    private final String uri;
    private final String path;
    private final QueryString queryString;
    private final Headers headers;
    private final Cookies cookies;
    private final Map<String, Object> lazyValues = new HashMap<>( 5 );

    public RequestHeaderInstance( String identity,
                                  String version, String method,
                                  String uri, String path, QueryString queryString,
                                  Headers headers, Cookies cookies )
    {
        this.identity = identity;
        this.version = version;
        this.method = method;
        this.uri = uri;
        this.path = path;
        this.queryString = queryString;
        this.headers = headers;
        this.cookies = cookies;
    }

    @SuppressWarnings( "unchecked" )
    private synchronized <T> T lazy( String key, Lazy<T> function )
    {
        if( !lazyValues.containsKey( key ) )
        {
            lazyValues.put( key, function.get() );
        }
        return (T) lazyValues.get( key );
    }

    @Override
    public String identity()
    {
        return identity;
    }

    @Override
    public String version()
    {
        return version;
    }

    @Override
    public String method()
    {
        return method;
    }

    @Override
    public String uri()
    {
        return uri;
    }

    @Override
    public String path()
    {
        return path;
    }

    @Override
    public QueryString queryString()
    {
        return queryString;
    }

    @Override
    public Headers headers()
    {
        return headers;
    }

    @Override
    public Cookies cookies()
    {
        return cookies;
    }

    @Override
    public String remoteAddress()
    {
        // TODO Implement X-Forwarded-For
        // Limitation on the loopback local address should be turned off by configuration
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String host()
    {
        return lazy( "host", new Lazy<String>()
        {
            @Override
            public String get()
            {
                return headers.singleValue( HOST );
            }
        } );
    }

    @Override
    public int port()
    {
        return lazy( "port", new Lazy< Integer>()
        {
            @Override
            public Integer get()
            {
                if( uri.startsWith( "http" ) )
                {
                    String parse = uri.substring( "https://".length() + 1 );
                    int slashIdx = parse.indexOf( '/' );
                    if( slashIdx < 0 )
                    {
                        return uri.startsWith( "https" ) ? DEFAULT_HTTPS_PORT : DEFAULT_HTTP_PORT;
                    }
                    parse = parse.substring( 0, slashIdx );
                    int colIdx = parse.indexOf( ':' );
                    return colIdx < 0
                           ? uri.startsWith( "https" ) ? DEFAULT_HTTPS_PORT : DEFAULT_HTTP_PORT
                           : Integer.valueOf( parse.substring( colIdx + 1, parse.length() ) );
                }
                else
                {
                    String host = headers.singleValue( HOST );
                    if( host.contains( ":" ) )
                    {
                        return Integer.valueOf( host.split( ":" )[1] );
                    }
                    return DEFAULT_HTTP_PORT;
                }
            }
        } );
    }

    @Override
    public String domain()
    {
        return lazy( "domain", new Lazy<String>()
        {
            @Override
            public String get()
            {
                String host = headers.singleValue( HOST );
                if( host.contains( ":" ) )
                {
                    return host.split( ":" )[0];
                }
                return host;
            }
        } );
    }

    @Override
    public String contentType()
    {
        return lazy( "contentType", new Lazy< String>()
        {
            @Override
            public String get()
            {
                String contentType = headers.singleValue( CONTENT_TYPE );
                if( isEmpty( contentType ) )
                {
                    return EMPTY;
                }
                return contentType.split( ";" )[0].toLowerCase( US );
            }
        } );
    }

    @Override
    public String charset()
    {
        return lazy( "charset", new Lazy<String>()
        {
            @Override
            public String get()
            {
                return extractCharset( headers.singleValue( CONTENT_TYPE ) );
            }
        } );
    }

}
