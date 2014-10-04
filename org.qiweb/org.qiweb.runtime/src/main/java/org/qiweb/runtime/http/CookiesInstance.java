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

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.qiweb.api.http.Cookies;
import org.qiweb.api.http.MutableCookies;
import org.qiweb.runtime.util.Comparators;
import org.qiweb.util.Strings;

import static org.qiweb.util.IllegalArguments.ensureNotEmpty;
import static org.qiweb.util.IllegalArguments.ensureNotNull;

/**
 * Cookies instance.
 */
public final class CookiesInstance
    implements MutableCookies, Serializable
{
    public static final Cookies EMPTY = new CookiesInstance();
    private final Map<String, Cookie> cookies;

    public CookiesInstance()
    {
        this.cookies = new TreeMap<>( Comparators.LOWER_CASE );
    }

    public CookiesInstance( Map<String, Cookie> cookies )
    {
        this();
        this.cookies.putAll( cookies );
    }

    @Override
    public boolean isEmpty()
    {
        return cookies.isEmpty();
    }

    @Override
    public boolean has( String name )
    {
        ensureNotEmpty( "Cookie Name", name );
        return cookies.containsKey( name );
    }

    @Override
    public Set<String> names()
    {
        return Collections.unmodifiableSet( cookies.keySet() );
    }

    @Override
    public Cookie get( String name )
    {
        ensureNotEmpty( "Cookie Name", name );
        return cookies.get( name );
    }

    @Override
    public String value( String name )
    {
        ensureNotEmpty( "Cookie Name", name );
        if( cookies.containsKey( name ) )
        {
            return cookies.get( name ).value();
        }
        return Strings.EMPTY;
    }

    @Override
    public MutableCookies set( String name, String value )
    {
        ensureNotEmpty( "Cookie Name", name );
        cookies.put(
            name,
            new CookieInstance(
                0,
                name, value == null ? Strings.EMPTY : value,
                Strings.EMPTY, Strings.EMPTY,
                Long.MIN_VALUE,
                false, true,
                Strings.EMPTY, Strings.EMPTY
            )
        );
        return this;
    }

    @Override
    public MutableCookies set( Cookie cookie )
    {
        ensureNotNull( "Cookie", cookie );
        cookies.put( cookie.name(), cookie );
        return this;
    }

    @Override
    public MutableCookies invalidate( String name )
    {
        ensureNotEmpty( "Cookie Name", name );
        // TODO Add expires NOW to remove the cookie from the browser asap
        // See http://stackoverflow.com/questions/5285940/correct-way-to-delete-cookies-server-side
        cookies.put(
            name,
            new CookieInstance(
                0,
                name, Strings.EMPTY,
                Strings.EMPTY, Strings.EMPTY,
                0,
                false, true,
                Strings.EMPTY, Strings.EMPTY
            )
        );
        return this;
    }

    @Override
    public Iterator<Cookie> iterator()
    {
        return Collections.unmodifiableCollection( cookies.values() ).iterator();
    }

    @Override
    public String toString()
    {
        return cookies.toString();
    }

    /**
     * Cookie instance.
     */
    public static class CookieInstance
        implements Cookie
    {
        private final int version;
        private final String name;
        private final String value;
        private final String path;
        private final String domain;
        private final long maxAge;
        private final boolean secure;
        private final boolean httpOnly;
        private final String comment;
        private final String commentUrl;

        public CookieInstance(
            int version,
            String name, String value,
            String path, String domain,
            long maxAge,
            boolean secure, boolean httpOnly,
            String comment, String commentUrl
        )
        {
            this.version = version;
            this.name = name;
            this.value = value;
            this.path = path;
            this.domain = domain;
            this.maxAge = maxAge;
            this.secure = secure;
            this.httpOnly = httpOnly;
            this.comment = comment;
            this.commentUrl = commentUrl;
        }

        @Override
        public int version()
        {
            return version;
        }

        @Override
        public String name()
        {
            return name;
        }

        @Override
        public String value()
        {
            return value;
        }

        @Override
        public String path()
        {
            return path;
        }

        @Override
        public String domain()
        {
            return domain;
        }

        @Override
        public long maxAge()
        {
            return maxAge;
        }

        @Override
        public boolean secure()
        {
            return secure;
        }

        @Override
        public boolean httpOnly()
        {
            return httpOnly;
        }

        @Override
        public String comment()
        {
            return comment;
        }

        @Override
        public String commentUrl()
        {
            return commentUrl;
        }

        @Override
        public String toString()
        {
            return "CookieInstance{"
                   + "version=" + version
                   + ", name=" + name
                   + ", value=" + value
                   + ", path=" + path
                   + ", domain=" + domain
                   + ", maxAge=" + maxAge
                   + ", secure=" + secure
                   + ", httpOnly=" + httpOnly
                   + ", comment=" + comment
                   + ", commentUrl=" + commentUrl
                   + '}';
        }

    }
}
