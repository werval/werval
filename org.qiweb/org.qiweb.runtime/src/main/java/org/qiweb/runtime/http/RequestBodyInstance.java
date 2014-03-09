/**
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
package org.qiweb.runtime.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.qiweb.api.http.FormAttributes;
import org.qiweb.api.http.FormUploads;
import org.qiweb.api.http.FormUploads.Upload;
import org.qiweb.api.http.RequestBody;
import org.qiweb.api.util.ByteSource;
import org.qiweb.api.util.Strings;

/**
 * A RequestBody Instance.
 */
public final class RequestBodyInstance
    implements RequestBody
{
    private final Charset charset;
    private final ByteSource bodyBytes;
    private final FormAttributes attributes;
    private final FormUploads uploads;

    /**
     * Create a new EMPTY RequestBody.
     * @param charset Body charset
     * @param allowMultiValuedAttributes Allow multi-valued attributes
     * @param allowMultiValuedUploads Allow multi-valued uploads
     */
    public RequestBodyInstance( Charset charset,
                                boolean allowMultiValuedAttributes, boolean allowMultiValuedUploads )
    {
        this.charset = charset;
        this.bodyBytes = null;
        this.attributes = new FormAttributesInstance( allowMultiValuedAttributes, Collections.<String, List<String>>emptyMap() );
        this.uploads = new FormUploadsInstance( allowMultiValuedUploads, Collections.<String, List<Upload>>emptyMap() );
    }

    /**
     * Create a new RequestBody backed by a ByteBuf.
     * 
     * @param charset Body charset
     * @param allowMultiValuedAttributes Allow multi-valued attributes
     * @param bodyBytes Body bytes
     */
    public RequestBodyInstance( Charset charset,
                                boolean allowMultiValuedAttributes,
                                ByteSource bodyBytes )
    {
        this.charset = charset;
        this.bodyBytes = bodyBytes;
        this.attributes = new FormAttributesInstance( allowMultiValuedAttributes, Collections.<String, List<String>>emptyMap() );
        this.uploads = new FormUploadsInstance( false, Collections.<String, List<Upload>>emptyMap() );
    }

    /**
     * Create a new RequestBody backed by form and upload data.
     * 
     * @param charset Body charset
     * @param allowMultiValuedAttributes Allow multi-valued attributes
     * @param allowMultiValuedUploads Allow multi-valued uploads
     * @param attributes Form attributes
     * @param uploads Upload data
     */
    public RequestBodyInstance( Charset charset,
                                boolean allowMultiValuedAttributes, boolean allowMultiValuedUploads,
                                Map<String, List<String>> attributes, Map<String, List<Upload>> uploads )
    {
        this.charset = charset;
        this.bodyBytes = null;
        this.attributes = new FormAttributesInstance( allowMultiValuedAttributes, attributes );
        this.uploads = new FormUploadsInstance( allowMultiValuedUploads, uploads );
    }

    @Override
    public FormAttributes formAttributes()
    {
        return attributes;
    }

    @Override
    public FormUploads formUploads()
    {
        return uploads;
    }

    @Override
    public InputStream asStream()
    {
        if( bodyBytes == null )
        {
            return new ByteArrayInputStream( new byte[ 0 ] );
        }
        return bodyBytes.asStream();
    }

    @Override
    public byte[] asBytes()
    {
        if( bodyBytes == null )
        {
            return new byte[ 0 ];
        }
        return bodyBytes.asBytes();
    }

    @Override
    public String asString()
    {
        return asString( charset );
    }

    @Override
    public String asString( Charset charset )
    {
        if( bodyBytes == null )
        {
            return Strings.EMPTY;
        }
        return bodyBytes.asString( charset );
    }
}
