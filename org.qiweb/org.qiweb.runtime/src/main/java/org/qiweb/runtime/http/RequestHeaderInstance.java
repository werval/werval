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
package org.qiweb.runtime.http;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.qiweb.api.http.Cookies;
import org.qiweb.api.http.Headers;
import org.qiweb.api.http.Method;
import org.qiweb.api.http.ProtocolVersion;
import org.qiweb.api.http.QueryString;
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.routes.ParameterBinders;
import org.qiweb.api.routes.Route;
import org.qiweb.api.util.Strings;
import org.qiweb.runtime.exceptions.BadRequestException;

import static java.util.Collections.emptyList;
import static java.util.Locale.US;
import static org.qiweb.api.http.Headers.Names.CONNECTION;
import static org.qiweb.api.http.Headers.Names.CONTENT_TYPE;
import static org.qiweb.api.http.Headers.Names.HOST;
import static org.qiweb.api.http.Headers.Names.X_FORWARDED_FOR;
import static org.qiweb.api.http.Headers.Values.CLOSE;
import static org.qiweb.api.http.Headers.Values.KEEP_ALIVE;
import static org.qiweb.api.util.Strings.EMPTY;
import static org.qiweb.api.util.Strings.isEmpty;
import static org.qiweb.runtime.http.HttpConstants.DEFAULT_HTTPS_PORT;
import static org.qiweb.runtime.http.HttpConstants.DEFAULT_HTTP_PORT;

public class RequestHeaderInstance
    implements RequestHeader
{
    /**
     * Extract Content-Type information from Content-Type string value removing options such as Charset.
     *
     * @param contentType Content-Type string value
     *
     * @return Extracted Content-Type or an empty String if absent
     */
    public static String extractContentType( String contentType )
    {
        if( isEmpty( contentType ) )
        {
            return EMPTY;
        }
        return contentType.split( ";" )[0].toLowerCase( US );
    }

    /**
     * Extract charset information from Content-Type string value.
     *
     * @param contentType Content-Type string value
     *
     * @return Extracted charset or an empty String if absent
     */
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
    private final String remoteSocketAddress;
    private final boolean xffEnabled;
    private final boolean xffCheckProxies;
    private final List<String> xffTrustedProxies;
    private final ProtocolVersion version;
    private final Method method;
    private final String uri;
    private final String path;
    private final QueryString queryString;
    private final Headers headers;
    private final Cookies cookies;
    private final Map<String, Object> parameters = new LinkedHashMap<>();

    public RequestHeaderInstance( String identity, String remoteSocketAddress,
                                  ProtocolVersion version, Method method,
                                  String uri, String path, QueryString queryString,
                                  Headers headers, Cookies cookies )
    {
        this(
            identity, remoteSocketAddress,
            false, false, emptyList(),
            version, method,
            uri, path, queryString,
            headers, cookies
        );
    }

    public RequestHeaderInstance( String identity, String remoteSocketAddress,
                                  boolean xffEnabled, boolean xffCheckProxies, List<String> xffTrustedProxies,
                                  ProtocolVersion version, Method method,
                                  String uri, String path, QueryString queryString,
                                  Headers headers, Cookies cookies )
    {
        this.identity = identity;
        this.remoteSocketAddress = remoteSocketAddress;
        this.xffEnabled = xffEnabled;
        this.xffCheckProxies = xffCheckProxies;
        this.xffTrustedProxies = xffTrustedProxies == null ? emptyList() : xffTrustedProxies;
        this.version = version;
        this.method = method;
        this.uri = uri;
        this.path = path;
        this.queryString = queryString;
        this.headers = headers;
        this.cookies = cookies;
    }

    @Override
    public String identity()
    {
        return identity;
    }

    @Override
    public ProtocolVersion version()
    {
        return version;
    }

    @Override
    public Method method()
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
        if( xffEnabled && headers.has( X_FORWARDED_FOR ) )
        {
            String xForwardedFor = headers.singleValue( X_FORWARDED_FOR );
            if( Strings.isEmpty( xForwardedFor ) )
            {
                throw new BadRequestException( X_FORWARDED_FOR + " header is empty." );
            }
            String[] proxyChain = xForwardedFor.split( "," );
            String remoteAddress = proxyChain[0].trim();
            if( xffCheckProxies )
            {
                if( proxyChain.length == 1 )
                {
                    throw new BadRequestException( X_FORWARDED_FOR + " header cannot be trusted." );
                }
                for( int idx = 1; idx < proxyChain.length; idx++ )
                {
                    String proxy = proxyChain[idx];
                    if( !xffTrustedProxies.contains( proxy.trim() ) )
                    {
                        throw new BadRequestException( X_FORWARDED_FOR + " header cannot be trusted." );
                    }
                }
            }
            return remoteAddress;
        }
        return remoteSocketAddress;
    }

    @Override
    public String host()
    {
        return headers.singleValue( HOST );
    }

    @Override
    public int port()
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

    @Override
    public String domain()
    {
        String host = headers.singleValue( HOST );
        if( host.contains( ":" ) )
        {
            return host.split( ":" )[0];
        }
        return host;
    }

    @Override
    public String contentType()
    {
        return extractContentType( headers.singleValue( CONTENT_TYPE ) );
    }

    @Override
    public String charset()
    {
        return extractCharset( headers.singleValue( CONTENT_TYPE ) );
    }

    @Override
    public boolean isKeepAlive()
    {
        String connection = headers.singleValue( CONNECTION );
        if( CLOSE.equalsIgnoreCase( connection ) )
        {
            return false;
        }
        if( version.isKeepAliveDefault() )
        {
            return true;
        }
        return KEEP_ALIVE.equalsIgnoreCase( connection );
    }

    @Override
    public RequestHeader bind( ParameterBinders parameterBinders, Route route )
    {
        parameters.clear();
        parameters.putAll( route.bindParameters( parameterBinders, path, queryString ) );
        return this;
    }

    @Override
    public Map<String, Object> parameters()
    {
        return Collections.unmodifiableMap( parameters );
    }
}
