/*
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

import io.werval.api.http.Headers;
import io.werval.api.http.MutableHeaders;
import io.werval.api.http.RequestHeader;
import io.werval.util.Strings;
import org.junit.Test;

import static io.werval.api.http.Headers.Names.CONTENT_TYPE;
import static io.werval.api.http.Headers.Names.HOST;
import static io.werval.api.http.ProtocolVersion.HTTP_1_1;
import static io.werval.api.mime.MimeTypesNames.APPLICATION_JSON;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class HttpRequestHeaderTest
{
    @Test
    public void testPort()
    {
        assertThat(
            withUri( "http://qiweb.org/" ).port(),
            equalTo( 80 ) );
        assertThat(
            withUri( "https://qiweb.org/" ).port(),
            equalTo( 443 ) );
        assertThat(
            withUri( "https://jdoe@qiweb.org/" ).port(),
            equalTo( 443 ) );
        assertThat(
            withUri( "http://qiweb.org:23023/" ).port(),
            equalTo( 23023 ) );
        assertThat(
            withUri( "http://qiweb.org:23023/download" ).port(),
            equalTo( 23023 ) );
        assertThat(
            withUri( "http://qiweb.org:23023/download?foo=bar#test:with:columns" ).port(),
            equalTo( 23023 ) );
        assertThat(
            withUri( "http://qiweb.org:23023/shorten?longUrl=http://qiweb.org" ).port(),
            equalTo( 23023 ) );
        assertThat(
            withUri( "http://127.0.0.1:23023/shorten?longUrl=http://qiweb.org/path" ).port(),
            equalTo( 23023 ) );
        assertThat(
            withUri( "http://localhost:23023/shorten?longUrl=http://qiweb.org:8080/path" ).port(),
            equalTo( 23023 ) );
    }

    @Test
    public void testDomain()
    {
        MutableHeaders headers = new HeadersInstance();
        assertThat(
            withHeaders( headers.withSingle( HOST, "qiweb.org" ) ).domain(),
            equalTo( "qiweb.org" ) );
        assertThat(
            withHeaders( headers.withSingle( HOST, "qiweb.org:23023" ) ).domain(),
            equalTo( "qiweb.org" ) );
    }

    @Test
    public void testContentType()
    {
        MutableHeaders headers = new HeadersInstance();
        assertThat(
            withHeaders( headers.withSingle( CONTENT_TYPE, APPLICATION_JSON ) ).contentType(),
            equalTo( "application/json" ) );
        assertThat(
            withHeaders( headers.withSingle( CONTENT_TYPE, APPLICATION_JSON + ";charset=utf-8" ) ).contentType(),
            equalTo( "application/json" ) );
    }

    @Test
    public void testCharset()
    {
        MutableHeaders headers = new HeadersInstance();
        assertThat(
            withHeaders( headers.withSingle( CONTENT_TYPE, APPLICATION_JSON ) ).charset(),
            equalTo( Strings.EMPTY ) );
        assertThat(
            withHeaders( headers.withSingle( CONTENT_TYPE, APPLICATION_JSON + ";charset=us-ascii" ) ).charset(),
            equalTo( "us-ascii" ) );
        assertThat(
            withHeaders( headers.withSingle( CONTENT_TYPE, APPLICATION_JSON + ";charset=utf-8" ) ).charset(),
            equalTo( "utf-8" ) );
        assertThat(
            withHeaders( headers.withSingle( CONTENT_TYPE, APPLICATION_JSON + ";charset=utf-8;foo=bar" ) ).charset(),
            equalTo( "utf-8" ) );
        assertThat(
            withHeaders( headers.withSingle( CONTENT_TYPE, APPLICATION_JSON + ";foo=bar;charset=utf-8" ) ).charset(),
            equalTo( "utf-8" ) );
    }

    private RequestHeader withUri( String uri )
    {
        return new RequestHeaderInstance(
            null, null, null, false, false, emptyList(),
            HTTP_1_1, null, uri, null, null, null, null
        );
    }

    private RequestHeader withHeaders( Headers headers )
    {
        return new RequestHeaderInstance(
            null, null, null, false, false, emptyList(),
            HTTP_1_1, null, null, null, null, headers, null
        );
    }
}
