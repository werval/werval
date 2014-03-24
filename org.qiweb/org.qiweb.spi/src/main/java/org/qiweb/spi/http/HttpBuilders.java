/*
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

import java.util.List;
import java.util.Map;
import org.qiweb.api.http.Cookies;
import org.qiweb.api.http.FormUploads;
import org.qiweb.api.http.Headers;
import org.qiweb.api.http.ProtocolVersion;
import org.qiweb.api.http.Request;
import org.qiweb.api.util.ByteSource;

/**
 * HTTP API Objects Builders.
 *
 * Use this to create instances of HTTP API Objects found in the {@link org.qiweb.api.http} package.
 * <p>
 * Typically used in unit tests running an Application
 * <p>
 * All builders are immutable and reusable.
 */
public interface HttpBuilders
{
    /**
     * Create a new Request Builder.
     *
     * HTTP Method and URI are mandatory.
     *
     * @return A newRequest Builder
     */
    RequestBuilder newRequestBuilder();

    /**
     * Request Builder.
     *
     * HTTP Method and URI are mandatory.
     */
    interface RequestBuilder
    {
        /**
         * Request identity.
         *
         * Default to {@literal NO_REQUEST_ID}.
         *
         * @param identity Request identity
         *
         * @return A new builder with the request identity set
         */
        RequestBuilder identifiedBy( String identity );

        /**
         * Remote socket address.
         *
         * Optional.
         *
         * @param remoteSocketAddress Remote socket address
         *
         * @return A new builder with the remote socket address set
         */
        RequestBuilder remoteSocketAddress( String remoteSocketAddress );

        /**
         * Protocol version.
         *
         * Default to {@link ProtocolVersion#HTTP_1_1}.
         *
         * @param version Protocol version
         *
         * @return A new builder with the protocol version set
         */
        RequestBuilder version( ProtocolVersion version );

        /**
         * HTTP method.
         *
         * Mandatory.
         *
         * @param method HTTP method
         *
         * @return A new builder with the method set
         */
        RequestBuilder method( String method );

        /**
         * URI.
         *
         * Mandatory.
         *
         * @param uri URI
         *
         * @return A new builder with the URI set
         */
        RequestBuilder uri( String uri );

        /**
         * Request headers.
         *
         * @param headers Request headers
         *
         * @return A new builder with the headers set
         */
        RequestBuilder headers( Headers headers );

        /**
         * Request headers.
         *
         * @param headers Request headers
         *
         * @return A new builder with the headers set
         */
        RequestBuilder headers( Map<String, List<String>> headers );

        /**
         * Request cookies.
         *
         * @param cookies Request cookies
         *
         * @return A new builder with the cookies set
         */
        RequestBuilder cookies( Cookies cookies );

        /**
         * Body bytes.
         *
         * @param bodyBytes Body bytes
         *
         * @return A new builder with the body set
         */
        RequestBuilder bodyBytes( ByteSource bodyBytes );

        /**
         * Body form and uploads.
         *
         *
         * @param attributes Form attributes
         * @param uploads    Multipart uploads
         *
         * @return A new builder with the body set
         */
        RequestBuilder bodyForm( Map<String, List<String>> attributes, Map<String, List<FormUploads.Upload>> uploads );

        /**
         * Build a new Request.
         *
         * @return A new Request
         *
         * @throws IllegalArgumentException if the builder state is not appropriate
         */
        Request build();
    }
}
