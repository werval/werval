/*
 * Copyright (c) 2013-2015 the original author or authors
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.werval.api.http.Cookies;
import io.werval.api.http.Headers;
import io.werval.api.http.Method;
import io.werval.api.http.ProtocolVersion;
import io.werval.api.http.QueryString;
import io.werval.api.http.Request;
import io.werval.api.http.RequestBody;
import io.werval.api.http.RequestHeader;
import io.werval.api.i18n.Lang;
import io.werval.api.mime.MediaRange;
import io.werval.api.routes.ParameterBinders;
import io.werval.api.routes.Route;

/**
 * Request Instance.
 */
public class RequestInstance
    implements Request
{
    private final RequestHeader header;
    private final RequestBody body;

    public RequestInstance( RequestHeader header, RequestBody body )
    {
        this.header = header;
        this.body = body;
    }

    @Override
    public String identity()
    {
        return header.identity();
    }

    @Override
    public ProtocolVersion version()
    {
        return header.version();
    }

    @Override
    public Method method()
    {
        return header.method();
    }

    @Override
    public String uri()
    {
        return header.uri();
    }

    @Override
    public String path()
    {
        return header.path();
    }

    @Override
    public QueryString queryString()
    {
        return header.queryString();
    }

    @Override
    public String remoteAddress()
    {
        return header.remoteAddress();
    }

    @Override
    public String host()
    {
        return header.host();
    }

    @Override
    public int port()
    {
        return header.port();
    }

    @Override
    public String domain()
    {
        return header.domain();
    }

    @Override
    public Optional<String> contentType()
    {
        return header.contentType();
    }

    @Override
    public Optional<String> charset()
    {
        return header.charset();
    }

    @Override
    public boolean isKeepAlive()
    {
        return header.isKeepAlive();
    }

    @Override
    public Headers headers()
    {
        return header.headers();
    }

    @Override
    public Cookies cookies()
    {
        return header.cookies();
    }

    @Override
    public Request bind( ParameterBinders parameterBinders, Route route )
    {
        header.bind( parameterBinders, route );
        return this;
    }

    @Override
    public Map<String, Object> parameters()
    {
        return header.parameters();
    }

    @Override
    public List<Lang> acceptedLangs()
    {
        return header.acceptedLangs();
    }

    @Override
    public Lang preferredLang()
    {
        return header.preferredLang();
    }

    @Override
    public List<MediaRange> acceptedMimeTypes()
    {
        return header.acceptedMimeTypes();
    }

    @Override
    public boolean acceptsMimeType( String mimeType )
    {
        return header.acceptsMimeType( mimeType );
    }

    @Override
    public String preferredMimeType( String... mimeTypes )
    {
        return header.preferredMimeType( mimeTypes );
    }

    @Override
    public RequestBody body()
    {
        return body;
    }
}
