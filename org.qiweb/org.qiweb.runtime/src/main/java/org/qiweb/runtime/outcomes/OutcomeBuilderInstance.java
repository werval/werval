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
package org.qiweb.runtime.outcomes;

import java.io.InputStream;
import java.nio.charset.Charset;
import org.qiweb.api.Config;
import org.qiweb.api.http.ResponseHeader;
import org.qiweb.api.mime.MimeTypes;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.api.outcomes.OutcomeBuilder;
import org.qiweb.util.ByteArrayByteSource;
import org.qiweb.util.ByteSource;

import static org.qiweb.api.http.Headers.Names.CONTENT_TYPE;
import static org.qiweb.api.mime.MimeTypesNames.APPLICATION_JSON;
import static org.qiweb.api.mime.MimeTypesNames.APPLICATION_XML;
import static org.qiweb.api.mime.MimeTypesNames.TEXT_HTML;
import static org.qiweb.api.mime.MimeTypesNames.TEXT_PLAIN;
import static org.qiweb.runtime.ConfigKeys.QIWEB_CHARACTER_ENCODING;
import static org.qiweb.runtime.ConfigKeys.QIWEB_HTTP_CHUNKSIZE;
import static org.qiweb.util.ByteSource.EMPTY_BYTES;

/**
 * Outcome Builder instance.
 */
public class OutcomeBuilderInstance
    implements OutcomeBuilder
{
    private final Charset defaultCharset;
    private final MimeTypes mimeTypes;
    private final ResponseHeader response;
    private Object body = EMPTY_BYTES;
    private long length = 0;
    private int chunkSize;

    /* package */ OutcomeBuilderInstance( Config config, MimeTypes mimeTypes, ResponseHeader response )
    {
        this.defaultCharset = config.charset( QIWEB_CHARACTER_ENCODING );
        this.mimeTypes = mimeTypes;
        this.response = response;
        this.chunkSize = config.intNumber( QIWEB_HTTP_CHUNKSIZE );
    }

    @Override
    public OutcomeBuilder withHeader( String name, String value )
    {
        response.headers().with( name, value );
        return this;
    }

    @Override
    public OutcomeBuilder as( String contentType )
    {
        response.headers().withSingle( CONTENT_TYPE, contentType );
        return this;
    }

    @Override
    public OutcomeBuilder asTextual( String contentType )
    {
        return as( mimeTypes.withCharsetOfTextual( contentType ) );
    }

    @Override
    public OutcomeBuilder asTextual( String contentType, Charset charset )
    {
        return as( mimeTypes.withCharset( contentType, charset ) );
    }

    @Override
    public OutcomeBuilder asTextPlain()
    {
        return as( mimeTypes.withCharsetOfTextual( TEXT_PLAIN ) );
    }

    @Override
    public OutcomeBuilder asTextPlain( Charset charset )
    {
        return as( mimeTypes.withCharset( TEXT_PLAIN, charset ) );
    }

    @Override
    public OutcomeBuilder asJson()
    {
        return as( mimeTypes.withCharsetOfTextual( APPLICATION_JSON ) );
    }

    @Override
    public OutcomeBuilder asJson( Charset charset )
    {
        return as( mimeTypes.withCharset( APPLICATION_JSON, charset ) );
    }

    @Override
    public OutcomeBuilder asXml()
    {
        return as( mimeTypes.withCharsetOfTextual( APPLICATION_XML ) );
    }

    @Override
    public OutcomeBuilder asXml( Charset charset )
    {
        return as( mimeTypes.withCharset( APPLICATION_XML, charset ) );
    }

    @Override
    public OutcomeBuilder asHtml()
    {
        return as( mimeTypes.withCharsetOfTextual( TEXT_HTML ) );
    }

    @Override
    public OutcomeBuilder asHtml( Charset charset )
    {
        return as( mimeTypes.withCharset( TEXT_HTML, charset ) );
    }

    @Override
    public OutcomeBuilder withBody( byte[] bodyBytes )
    {
        body = new ByteArrayByteSource( bodyBytes );
        length = bodyBytes.length;
        return this;
    }

    @Override
    public OutcomeBuilder withBody( CharSequence bodyChars )
    {
        return withBody( bodyChars, defaultCharset );
    }

    @Override
    public OutcomeBuilder withBody( CharSequence bodyChars, Charset charset )
    {
        byte[] bodyBytes = bodyChars.toString().getBytes( charset );
        body = new ByteArrayByteSource( bodyBytes );
        length = bodyBytes.length;
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
            return new SimpleOutcome( response );
        }
        if( body instanceof ByteSource )
        {
            ByteSource bodyByteSource = (ByteSource) body;
            return new SimpleOutcome( response ).withEntity( bodyByteSource );
        }
        if( body instanceof InputStream )
        {
            InputStream bodyInputStream = (InputStream) body;
            if( length != -1 )
            {
                return new InputStreamOutcome( response, bodyInputStream, length );
            }
            return new ChunkedInputOutcome( response, bodyInputStream, chunkSize );
        }
        throw new UnsupportedOperationException( "Unsupported body type ( " + body.getClass() + " ) " + body );
    }
}
