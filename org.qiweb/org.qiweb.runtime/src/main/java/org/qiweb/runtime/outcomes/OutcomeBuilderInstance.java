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
package org.qiweb.runtime.outcomes;

import io.netty.buffer.ByteBuf;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.qiweb.api.Config;
import org.qiweb.api.http.MutableCookies;
import org.qiweb.api.http.MutableHeaders;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.api.outcomes.OutcomeBuilder;

import static io.netty.buffer.Unpooled.EMPTY_BUFFER;
import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.buffer.Unpooled.wrappedBuffer;
import static org.qiweb.api.http.Headers.Names.CONTENT_TYPE;
import static org.qiweb.runtime.ConfigKeys.QIWEB_CHARACTER_ENCODING;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_CHUNKSIZE;

/**
 * Outcome Builder instance.
 */
public class OutcomeBuilderInstance
    implements OutcomeBuilder
{

    private final int status;
    private final MutableHeaders headers;
    private final MutableCookies cookies;
    private Object body = EMPTY_BUFFER;
    private long length = 0;
    private int chunkSize;
    private final Charset defaultCharset;


    /* package */ OutcomeBuilderInstance( int status, Config config, MutableHeaders headers, MutableCookies cookies )
    {
        this.status = status;
        this.headers = headers;
        this.cookies = cookies;
        this.chunkSize = config.intNumber( QIWEB_HTTP_CHUNKSIZE );
        this.defaultCharset = config.charset( QIWEB_CHARACTER_ENCODING );
    }

    @Override
    public OutcomeBuilder withHeader( String name, String value )
    {
        headers.with( name, value );
        return this;
    }

    @Override
    public OutcomeBuilder as( String contentType )
    {
        headers.withSingle( CONTENT_TYPE, contentType );
        return this;
    }

    @Override
    public OutcomeBuilder withBody( byte[] bodyBytes )
    {
        ByteBuf buffer = wrappedBuffer( bodyBytes );
        body = buffer;
        length = buffer.readableBytes();
        return this;
    }

    @Override
    public OutcomeBuilder withBody( String bodyString )
    {
        return withBody( bodyString, defaultCharset );
    }

    @Override
    public OutcomeBuilder withBody( String bodyString, Charset charset )
    {
        ByteBuf buffer = copiedBuffer( bodyString, charset );
        body = buffer;
        length = buffer.readableBytes();
        return this;
    }

    @Override
    public OutcomeBuilder withBody( InputStream bodyInputStream )
    {
        body = bodyInputStream;
        length = -1;
        return this;
    }

    @Override
    public OutcomeBuilder withBody( InputStream bodyInputStream, int overridenChunkSize )
    {
        body = bodyInputStream;
        length = -1;
        chunkSize = overridenChunkSize;
        return this;
    }

    @Override
    public OutcomeBuilder withBody( InputStream bodyInputStream, long bodyLength )
    {
        body = bodyInputStream;
        length = bodyLength;
        return this;
    }

    @Override
    public Outcome build()
    {
        if( body == null )
        {
            return new SimpleOutcome( status, headers );
        }
        if( body instanceof ByteBuf )
        {
            ByteBuf bodyByteBuf = (ByteBuf) body;
            return new SimpleOutcome( status, headers ).withEntity( bodyByteBuf );
        }
        if( body instanceof InputStream )
        {
            InputStream bodyInputStream = (InputStream) body;
            if( length != -1 )
            {
                return new InputStreamOutcome( status, headers, bodyInputStream, length );
            }
            return new ChunkedInputOutcome( status, headers, bodyInputStream, chunkSize );
        }
        throw new UnsupportedOperationException( "Unsupported body type ( " + body.getClass() + " ) " + body );
    }

}
