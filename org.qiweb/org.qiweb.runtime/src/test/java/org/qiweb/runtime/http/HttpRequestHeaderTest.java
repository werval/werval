package org.qiweb.runtime.http;

import org.codeartisans.java.toolbox.Strings;
import org.junit.Test;
import org.qiweb.api.http.Headers;
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.http.MutableHeaders;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.qiweb.api.http.Headers.Names.HOST;
import static org.qiweb.api.http.Headers.Names.CONTENT_TYPE;

public class HttpRequestHeaderTest
{

    @Test
    public void testPort()
    {
        assertThat( withUri( "http://qiweb.org/" ).port(), equalTo( 80 ) );
        assertThat( withUri( "https://qiweb.org/" ).port(), equalTo( 443 ) );
        assertThat( withUri( "https://jdoe@qiweb.org/" ).port(), equalTo( 443 ) );
        assertThat( withUri( "http://qiweb.org:23023/" ).port(), equalTo( 23023 ) );
        assertThat( withUri( "http://qiweb.org:23023/download" ).port(), equalTo( 23023 ) );
        assertThat( withUri( "http://qiweb.org:23023/download?foo=bar#test:with:columns" ).port(), equalTo( 23023 ) );
    }

    @Test
    public void testDomain()
    {
        MutableHeaders headers = new HeadersInstance( false );
        assertThat( withHeaders( headers.withSingle( HOST, "qiweb.org" ) ).domain(), equalTo( "qiweb.org" ) );
        assertThat( withHeaders( headers.withSingle( HOST, "qiweb.org:23023" ) ).domain(), equalTo( "qiweb.org" ) );
    }

    @Test
    public void testContentType()
    {
        MutableHeaders headers = new HeadersInstance( false );
        assertThat( withHeaders( headers.withSingle( CONTENT_TYPE, "application/json" ) ).contentType(), equalTo( "application/json" ) );
        assertThat( withHeaders( headers.withSingle( CONTENT_TYPE, "application/json;charset=utf-8" ) ).contentType(), equalTo( "application/json" ) );
    }

    @Test
    public void testCharset()
    {
        MutableHeaders headers = new HeadersInstance( false );
        assertThat( withHeaders( headers.withSingle( CONTENT_TYPE, "application/json" ) ).charset(), equalTo( Strings.EMPTY ) );
        assertThat( withHeaders( headers.withSingle( CONTENT_TYPE, "application/json;charset=utf-8" ) ).charset(), equalTo( "utf-8" ) );
        assertThat( withHeaders( headers.withSingle( CONTENT_TYPE, "application/json;charset=utf-8;foo=bar" ) ).charset(), equalTo( "utf-8" ) );
        assertThat( withHeaders( headers.withSingle( CONTENT_TYPE, "application/json;foo=bar;charset=utf-8" ) ).charset(), equalTo( "utf-8" ) );
    }

    private RequestHeader withUri( String uri )
    {
        return new RequestHeaderInstance( null, null, null, uri, null, null, null, null );
    }

    private RequestHeader withHeaders( Headers headers )
    {
        return new RequestHeaderInstance( null, null, null, null, null, null, headers, null );
    }
}
