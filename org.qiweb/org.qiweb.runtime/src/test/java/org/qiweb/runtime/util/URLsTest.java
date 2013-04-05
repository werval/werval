package org.qiweb.runtime.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class URLsTest
{

    @Test
    @SuppressWarnings( "unchecked" )
    public void testAppendQueryString()
    {
        String url = "http://acme.com/foo";

        assertThat( URLs.appendQueryString( url, null ), equalTo( "http://acme.com/foo" ) );
        assertThat( URLs.appendQueryString( url, EMPTY_MAP ), equalTo( "http://acme.com/foo" ) );

        Map<String, List<String>> data = new LinkedHashMap<>();
        data.put( "foo", new ArrayList<String>() );
        data.get( "foo" ).add( "bar" );
        assertThat( URLs.appendQueryString( url, data ), equalTo( "http://acme.com/foo?foo=bar" ) );

        data.get( "foo" ).add( "bazar" );
        assertThat( URLs.appendQueryString( url, data ), equalTo( "http://acme.com/foo?foo=bar&foo=bazar" ) );

        url = "http://acme.com/foo?foo=bar";
        assertThat( URLs.appendQueryString( url, data ), equalTo( "http://acme.com/foo?foo=bar&foo=bar&foo=bazar" ) );

        url = "http://acme.com/foo?foo=bar#cathedral";
        assertThat( URLs.appendQueryString( url, data ), equalTo( "http://acme.com/foo?foo=bar&foo=bar&foo=bazar#cathedral" ) );

    }
}
