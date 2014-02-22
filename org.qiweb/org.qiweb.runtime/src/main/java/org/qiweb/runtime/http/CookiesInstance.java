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

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.qiweb.api.http.MutableCookies;
import org.qiweb.api.util.Strings;
import org.qiweb.runtime.util.Comparators;

import static org.qiweb.api.exceptions.NullArgumentException.ensureNotEmpty;

public class CookiesInstance
    implements MutableCookies
{
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
        // TODO Implement serious Cookie creation
        cookies.put( name, new CookieInstance( name, "", "", false, value == null ? Strings.EMPTY : value, true ) );
        return this;
    }

    @Override
    public MutableCookies invalidate( String name )
    {
        ensureNotEmpty( "Cookie Name", name );
        // TODO Add expires NOW to remove the cookie from the browser asap
        // See http://stackoverflow.com/questions/5285940/correct-way-to-delete-cookies-server-side
        cookies.put( name, new CookieInstance( name, Strings.EMPTY, Strings.EMPTY, false, Strings.EMPTY, true ) );
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

    public static class CookieInstance
        implements Cookie
    {

        private final String name;
        private final String path;
        private final String domain;
        private final boolean secure;
        private final String value;
        private final boolean httpOnly;

        public CookieInstance( String name, String path, String domain, boolean secure, String value, boolean httpOnly )
        {
            this.name = name;
            this.path = path;
            this.domain = domain;
            this.secure = secure;
            this.value = value;
            this.httpOnly = httpOnly;
        }

        @Override
        public String name()
        {
            return name;
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
        public boolean secure()
        {
            return secure;
        }

        @Override
        public String value()
        {
            return value;
        }

        @Override
        public boolean httpOnly()
        {
            return httpOnly;
        }

        @Override
        public String toString()
        {
            return "Cookie{" + "name=" + name + ", path=" + path + ", domain=" + domain + ", secure=" + secure + ", value=" + value + ", httpOnly=" + httpOnly + '}';
        }
    }
}
