/**
 * Copyright (c) 2014 the original author or authors
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
package org.qiweb.spi.http;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import org.qiweb.api.http.Cookies;
import org.qiweb.api.http.FormUploads;
import org.qiweb.api.http.Headers;
import org.qiweb.api.http.ProtocolVersion;
import org.qiweb.api.http.QueryString;
import org.qiweb.api.http.RequestBody;
import org.qiweb.api.http.RequestHeader;
import org.qiweb.api.util.ByteSource;

/**
 * HTTP API Objects Builders.
 * <p>Use this to create instances of HTTP API Objects found in the {@link org.qiweb.api.http} package.</p>
 * <p>All builders are immutable and reusable.</p>
 */
public interface HttpBuilders
{
    QueryStringBuilder newQueryStringBuilder();

    HeadersBuilder newHeadersBuilder();

    RequestHeaderBuilder newRequestHeaderBuilder();

    RequestBodyBuilder newRequestBodyBuilder();

    interface QueryStringBuilder
    {
        QueryStringBuilder parameters( Map<String, List<String>> parameters );

        QueryString build();
    }

    interface HeadersBuilder
    {
        HeadersBuilder headers( Map<String, List<String>> headers );

        Headers build();
    }

    interface RequestHeaderBuilder
    {
        RequestHeaderBuilder identifiedBy( String identity );

        RequestHeaderBuilder remoteSocketAddress( String remoteSocketAddress );

        RequestHeaderBuilder version( ProtocolVersion version );

        RequestHeaderBuilder method( String method );

        RequestHeaderBuilder uri( String uri );

        RequestHeaderBuilder path( String path );

        RequestHeaderBuilder queryString( QueryString queryString );

        RequestHeaderBuilder headers( Headers headers );

        RequestHeaderBuilder cookies( Cookies cookies );

        RequestHeader build();
    }

    interface RequestBodyBuilder
    {
        RequestBodyBuilder charset( Charset charset );

        RequestBodyBuilder bytes( ByteSource bodyBytes );

        RequestBodyBuilder form( Map<String, List<String>> attributes, Map<String, List<FormUploads.Upload>> uploads );

        RequestBody build();
    }
}
