package org.qiweb.runtime.http;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.codeartisans.java.toolbox.Strings;
import org.qi4j.functional.Function;
import org.qiweb.api.http.Headers;
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.http.QueryString;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;

public class RequestHeaderInstance
    implements RequestHeader
{

    private final String identity;
    private final String version;
    private final String method;
    private final String uri;
    private final String path;
    private final QueryString queryString;
    private final Headers headers;
    private final boolean hasBody;
    private Map<String, Object> lazyValues = new HashMap<>();

    @SuppressWarnings( "unchecked" )
    private synchronized <T> T lazy( String key, Function<Void, T> function )
    {
        if( !lazyValues.containsKey( key ) )
        {
            lazyValues.put( key, function.map( null ) );
        }
        return (T) lazyValues.get( key );
    }

    public RequestHeaderInstance( String identity,
                                  String version, String method,
                                  String uri, String path, QueryString queryString,
                                  Headers headers,
                                  boolean hasBody )
    {
        this.identity = identity;
        this.version = version;
        this.method = method;
        this.uri = uri;
        this.path = path;
        this.queryString = queryString;
        this.headers = headers;
        this.hasBody = hasBody;
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
    public boolean hasBody()
    {
        return hasBody;
    }

    @Override
    public String remoteAddress()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public String host()
    {
        return lazy( "host", new Function<Void, String>()
        {
            @Override
            public String map( Void from )
            {
                return headers.valueOf( HOST );
            }
        } );
    }

    @Override
    public int port()
    {
        return lazy( "port", new Function<Void, Integer>()
        {
            @Override
            public Integer map( Void from )
            {
                String parse = uri.substring( 9 );
                parse = parse.substring( 0, parse.indexOf( '/' ) );
                int colIdx = parse.indexOf( ':' );
                if( colIdx < 0 )
                {
                    if( uri.startsWith( "https" ) )
                    {
                        return 443;
                    }
                    else
                    {
                        return 80;
                    }
                }
                else
                {
                    return Integer.valueOf( parse.substring( colIdx + 1, parse.length() ) );
                }
            }
        } );
    }

    @Override
    public String domain()
    {
        return lazy( "domain", new Function<Void, String>()
        {
            @Override
            public String map( Void from )
            {
                return headers.valueOf( HOST ).split( ":" )[0];
            }
        } );
    }

    @Override
    public String contentType()
    {
        return lazy( "contentType", new Function<Void, String>()
        {
            @Override
            public String map( Void from )
            {
                return headers.valueOf( CONTENT_TYPE ).split( ";" )[0].toLowerCase( Locale.US );
            }
        } );
    }

    @Override
    public String charset()
    {
        return lazy( "charset", new Function<Void, String>()
        {
            @Override
            public String map( Void from )
            {
                String[] split = headers.valueOf( CONTENT_TYPE ).split( ";" );
                if( split.length <= 1 )
                {
                    return Strings.EMPTY;
                }
                for( int idx = 1; idx < split.length; idx++ )
                {
                    String option = split[idx].trim().toLowerCase( Locale.US );
                    if( option.startsWith( "charset" ) )
                    {
                        return option.split( "=" )[1];
                    }
                }
                return headers.valueOf( CONTENT_TYPE ).split( ";" )[0].toLowerCase( Locale.US );
            }
        } );
    }

    @Override
    public RequestHeader clone()
    {
        return new RequestHeaderInstance( identity, version, method, uri, path, queryString, headers, hasBody );
    }
}
