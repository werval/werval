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

import io.werval.api.Config;
import io.werval.api.Crypto;
import io.werval.api.http.Cookies.Cookie;
import io.werval.api.http.Session;
import io.werval.util.Strings;
import io.werval.util.URLs;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.qiweb.runtime.http.CookiesInstance.CookieInstance;
import org.qiweb.runtime.util.Comparators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qiweb.runtime.ConfigKeys.APP_SESSION_COOKIE_DOMAIN;
import static org.qiweb.runtime.ConfigKeys.APP_SESSION_COOKIE_HTTPONLY;
import static org.qiweb.runtime.ConfigKeys.APP_SESSION_COOKIE_NAME;
import static org.qiweb.runtime.ConfigKeys.APP_SESSION_COOKIE_PATH;
import static org.qiweb.runtime.ConfigKeys.APP_SESSION_COOKIE_SECURE;
import static org.qiweb.runtime.ConfigKeys.QIWEB_CHARACTER_ENCODING;

/**
 * Session instance.
 */
public final class SessionInstance
    implements Session
{
    private static final Logger LOG = LoggerFactory.getLogger( SessionInstance.class );
    private static final Pattern COOKIE_VALUE_PATTERN = Pattern.compile( "\u0000([^:]*):([^\u0000]*)\u0000" );
    private final Config config;
    private final Crypto crypto;
    private final Map<String, String> session;
    private boolean changed = false;

    public SessionInstance( Config config, Crypto crypto )
    {
        this.config = config;
        this.crypto = crypto;
        this.session = new TreeMap<>( Comparators.LOWER_CASE );
    }

    public SessionInstance( Config config, Crypto crypto, Map<String, String> session )
    {
        this( config, crypto );
        this.session.putAll( session );
    }

    public SessionInstance( Config config, Crypto crypto, Cookie cookie )
    {
        this( config, crypto );
        if( cookie == null || Strings.isEmpty( cookie.value() ) )
        {
            return;
        }
        String[] splitted = cookie.value().split( "-", 2 );
        if( splitted.length != 2 )
        {
            LOG.warn( "Invalid Session Cookie Value: '{}'. Will use an empty Session.", cookie.value() );
            return;
        }
        String signature = splitted[0];
        String payload = splitted[1];
        if( !signature.equals( crypto.hmacSha256Hex( payload ) ) )
        {
            LOG.warn( "Invalid Session Cookie Signature: '{}'. Will use an empty Session.", cookie.value() );
            return;
        }
        String decoded = URLs.decode( payload, config.charset( QIWEB_CHARACTER_ENCODING ) );
        Matcher matcher = COOKIE_VALUE_PATTERN.matcher( decoded );
        while( matcher.find() )
        {
            session.put( matcher.group( 1 ), matcher.group( 2 ) );
        }
    }

    @Override
    public boolean hasChanged()
    {
        return changed;
    }

    @Override
    public boolean has( String key )
    {
        return session.containsKey( key );
    }

    @Override
    public String get( String key )
    {
        return session.get( key );
    }

    @Override
    public void set( String key, String value )
    {
        if( key.contains( ":" ) )
        {
            throw new IllegalArgumentException( "Character ':' is not allowed in a session key." );
        }
        changed = true;
        if( value == null )
        {
            session.remove( key );
        }
        else
        {
            session.put( key, value );
        }
    }

    @Override
    public String remove( String key )
    {
        changed = true;
        return session.remove( key );
    }

    @Override
    public void clear()
    {
        changed = true;
        session.clear();
    }

    @Override
    public Map<String, String> asMap()
    {
        return Collections.unmodifiableMap( session );
    }

    @Override
    public Cookie signedCookie()
    {
        StringBuilder sb = new StringBuilder();
        for( Entry<String, String> entry : session.entrySet() )
        {
            sb.append( "\u0000" ).append( entry.getKey() ).append( ":" ).append( entry.getValue() ).append( "\u0000" );
        }
        String sessionData = URLs.encode( sb.toString(), config.charset( QIWEB_CHARACTER_ENCODING ) );
        String signedCookieValue = crypto.hmacSha256Hex( sessionData ) + "-" + sessionData;
        return new CookieInstance(
            0,
            config.string( APP_SESSION_COOKIE_NAME ),
            signedCookieValue,
            config.string( APP_SESSION_COOKIE_PATH ),
            config.has( APP_SESSION_COOKIE_DOMAIN ) ? config.string( APP_SESSION_COOKIE_DOMAIN ) : null,
            Long.MIN_VALUE,
            config.bool( APP_SESSION_COOKIE_SECURE ),
            config.bool( APP_SESSION_COOKIE_HTTPONLY ),
            null,
            null
        );
    }
}
