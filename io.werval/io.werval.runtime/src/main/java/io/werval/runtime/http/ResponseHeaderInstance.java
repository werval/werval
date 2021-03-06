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
package io.werval.runtime.http;

import java.io.Serializable;

import io.werval.api.http.MutableCookies;
import io.werval.api.http.MutableHeaders;
import io.werval.api.http.ProtocolVersion;
import io.werval.api.http.ResponseHeader;
import io.werval.api.http.Status;

import static io.werval.api.http.Headers.Names.CONNECTION;
import static io.werval.api.http.Headers.Values.KEEP_ALIVE;

/**
 * Response Header Instance.
 */
public class ResponseHeaderInstance
    implements ResponseHeader, Serializable
{
    private final MutableHeaders headers;
    private final MutableCookies cookies;
    private ProtocolVersion version;
    private Status status = Status.valueOf( 0 );

    /* serialization */ ResponseHeaderInstance()
    {
        this( ProtocolVersion.HTTP_1_1 );
    }

    public ResponseHeaderInstance( ProtocolVersion version )
    {
        this.version = version;
        this.headers = new HeadersInstance();
        this.cookies = new CookiesInstance();
    }

    @Override
    public ProtocolVersion version()
    {
        return version;
    }

    public ResponseHeader withVersion( ProtocolVersion version )
    {
        this.version = version;
        return this;
    }

    @Override
    public Status status()
    {
        return status;
    }

    public ResponseHeader withStatus( Status status )
    {
        this.status = status;
        return this;
    }

    public ResponseHeader withStatus( int code )
    {
        this.status = Status.valueOf( code );
        return this;
    }

    public ResponseHeader withStatus( int code, String reasonPhrase )
    {
        this.status = new Status( code, reasonPhrase );
        return this;
    }

    @Override
    public boolean isKeepAlive()
    {
        return KEEP_ALIVE.equalsIgnoreCase( headers.singleValue( CONNECTION ) );
    }

    @Override
    public MutableHeaders headers()
    {
        return headers;
    }

    @Override
    public MutableCookies cookies()
    {
        return cookies;
    }
}
