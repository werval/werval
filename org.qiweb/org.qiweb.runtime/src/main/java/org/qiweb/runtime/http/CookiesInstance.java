package org.qiweb.runtime.http;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.codeartisans.java.toolbox.Strings;
import org.qiweb.api.http.Cookies;
import org.qiweb.runtime.util.Comparators;

public class CookiesInstance
    implements Cookies
{

    private final Map<String, Cookie> cookies;

    public CookiesInstance()
    {
        this.cookies = new TreeMap<>( Comparators.LOWER_CASE );
    }

    public CookiesInstance( Map<String, Cookie> cookies )
    {
        this.cookies = cookies;
    }

    @Override
    public Set<String> names()
    {
        return Collections.unmodifiableSet( cookies.keySet() );
    }

    @Override
    public Cookie get( String name )
    {
        return cookies.get( name );
    }

    @Override
    public String valueOf( String name )
    {
        if( cookies.containsKey( name ) )
        {
            return cookies.get( name ).value();
        }
        return Strings.EMPTY;
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
