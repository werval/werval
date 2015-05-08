/*
 * Copyright (c) 2013-2015 the original author or authors
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
package io.werval.runtime.http;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.werval.api.http.Cookies;
import io.werval.api.http.Headers;
import io.werval.api.http.Method;
import io.werval.api.http.ProtocolVersion;
import io.werval.api.http.QueryString;
import io.werval.api.http.RequestHeader;
import io.werval.api.i18n.Lang;
import io.werval.api.i18n.Langs;
import io.werval.api.mime.MediaRange;
import io.werval.api.routes.ParameterBinders;
import io.werval.api.routes.Route;
import io.werval.runtime.exceptions.BadRequestException;
import io.werval.runtime.mime.MediaRangeInstance;
import io.werval.util.Couple;
import io.werval.util.Strings;

import static io.werval.api.http.Headers.Names.ACCEPT;
import static io.werval.api.http.Headers.Names.ACCEPT_LANGUAGE;
import static io.werval.api.http.Headers.Names.CONNECTION;
import static io.werval.api.http.Headers.Names.CONTENT_TYPE;
import static io.werval.api.http.Headers.Names.HOST;
import static io.werval.api.http.Headers.Names.X_FORWARDED_FOR;
import static io.werval.api.http.Headers.Values.CLOSE;
import static io.werval.api.http.Headers.Values.KEEP_ALIVE;
import static io.werval.runtime.http.HttpConstants.DEFAULT_HTTPS_PORT;
import static io.werval.runtime.http.HttpConstants.DEFAULT_HTTP_PORT;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;

/**
 * RequestHeader instance.
 */
public class RequestHeaderInstance
    implements RequestHeader
{
    private final Langs langs;
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

    public RequestHeaderInstance(
        Langs langs,
        String identity, String remoteSocketAddress,
        boolean xffEnabled, boolean xffCheckProxies, List<String> xffTrustedProxies,
        ProtocolVersion version, Method method,
        String uri, String path, QueryString queryString,
        Headers headers, Cookies cookies
    )
    {
        this.langs = langs;
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
                    throw new BadRequestException( X_FORWARDED_FOR + " header cannot be trusted, no proxy in chain." );
                }
                for( int idx = 1; idx < proxyChain.length; idx++ )
                {
                    String proxy = proxyChain[idx].trim();
                    if( !xffTrustedProxies.contains( proxy ) )
                    {
                        throw new BadRequestException(
                            X_FORWARDED_FOR + " header cannot be trusted, untrusted proxy in chain: " + proxy
                        );
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
    public Optional<String> contentType()
    {
        return headers.singleValueOptional( CONTENT_TYPE )
            .map( Headers::extractContentMimeType )
            .orElse( Optional.empty() );
    }

    @Override
    public Optional<String> charset()
    {
        return headers.singleValueOptional( CONTENT_TYPE )
            .map( Headers::extractCharset )
            .orElse( Optional.empty() );
    }

    @Override
    public boolean isKeepAlive()
    {
        Optional<String> connection = headers.singleValueOptional( CONNECTION );
        if( connection.isPresent() && CLOSE.equalsIgnoreCase( connection.get() ) )
        {
            return false;
        }
        if( version.isKeepAliveDefault() )
        {
            return true;
        }
        return connection.isPresent() && KEEP_ALIVE.equalsIgnoreCase( connection.get() );
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
        return unmodifiableMap( parameters );
    }

    @Override
    public List<Lang> acceptedLangs()
    {
        SortedSet<Couple<Double, String>> parsed = new TreeSet<>( (o1, o2) -> o2.left().compareTo( o1.left() ) );
        parsed.addAll( parseAcceptHeader( ACCEPT_LANGUAGE ) );
        return parsed.stream().map( e -> langs.fromCode( e.right() ) ).collect( toList() );
    }

    @Override
    public Lang preferredLang()
    {
        return langs.preferred( acceptedLangs() );
    }

    @Override
    public List<MediaRange> acceptedMimeTypes()
    {
        return MediaRangeInstance.parseList( Strings.join( headers.values( ACCEPT ), "," ) );
    }

    @Override
    public boolean acceptsMimeType( String mimeType )
    {
        return MediaRangeInstance.accepts( acceptedMimeTypes(), mimeType );
    }

    @Override
    public String preferredMimeType( String... mimeTypes )
    {
        return MediaRangeInstance.preferred( acceptedMimeTypes(), mimeTypes );
    }

    /**
     * Pattern that match q-values of {@literal Accept*} headers.
     */
    private static final Pattern Q_PATTERN = Pattern.compile( ";\\s*q=([0-9.]+)" );

    /**
     * Parse {@literal Accept*} headers.
     *
     * @param acceptHeaderName Any {@literal Accept*} header name
     *
     * @return Parsed {@literal Accept*} header items with their q-values
     */
    private List<Couple<Double, String>> parseAcceptHeader( String acceptHeaderName )
    {
        if( !headers.has( acceptHeaderName ) )
        {
            return emptyList();
        }
        List<Couple<Double, String>> parsed = new ArrayList<>();
        List<String> acceptLangHeaders = headers.values( acceptHeaderName );
        for( String acceptLangHeader : acceptLangHeaders )
        {
            for( String acceptLang : acceptLangHeader.split( "," ) )
            {
                Matcher matcher = Q_PATTERN.matcher( acceptLang );
                if( matcher.find() )
                {
                    parsed.add( Couple.of(
                        Double.valueOf( matcher.group( 1 ) ),
                        acceptLang.substring( 0, matcher.start() ).trim()
                    ) );
                }
                else
                {
                    parsed.add( Couple.of( 1D, acceptLang.trim() ) );
                }
            }
        }
        return parsed;
    }
}
