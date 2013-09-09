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
package org.qiweb.runtime.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.codeartisans.java.toolbox.Strings;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.http.FormAttributes;
import org.qiweb.api.http.FormUploads;
import org.qiweb.api.http.FormUploads.Upload;
import org.qiweb.api.http.RequestBody;
import org.qiweb.runtime.util.FileByteBuff;

import static io.netty.util.CharsetUtil.UTF_8;

/**
 * A RequestBody Instance.
 */
public final class RequestBodyInstance
    implements RequestBody
{

    private final ByteBuf byteBuf;
    private final FormAttributes attributes;
    private final FormUploads uploads;

    /**
     * Create a new EMPTY RequestBody.
     */
    public RequestBodyInstance()
    {
        this.byteBuf = null;
        this.attributes = new FormAttributesInstance( Collections.<String, List<String>>emptyMap() );
        this.uploads = new FormUploadsInstance( Collections.<String, List<Upload>>emptyMap() );
    }

    /**
     * Create a new RequestBody backed by a ByteBuf.
     * 
     * @param byteBuf Body data
     */
    public RequestBodyInstance( ByteBuf byteBuf )
    {
        this.byteBuf = byteBuf;
        this.attributes = new FormAttributesInstance( Collections.<String, List<String>>emptyMap() );
        this.uploads = new FormUploadsInstance( Collections.<String, List<Upload>>emptyMap() );
    }

    /**
     * Create a new RequestBody backed by form and upload data.
     * 
     * @param attributes Form attributes
     * @param uploads Upload data
     */
    public RequestBodyInstance( Map<String, List<String>> attributes, Map<String, List<Upload>> uploads )
    {
        this.byteBuf = null;
        this.attributes = new FormAttributesInstance( attributes );
        this.uploads = new FormUploadsInstance( uploads );
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
        if( byteBuf == null )
        {
            return new ByteArrayInputStream( new byte[ 0 ] );
        }
        if( byteBuf instanceof FileByteBuff )
        {
            return ( (FileByteBuff) byteBuf ).getInputStream();
        }
        return new ByteBufInputStream( byteBuf );
    }

    @Override
    public byte[] asBytes()
    {
        if( byteBuf == null )
        {
            return new byte[ 0 ];
        }
        if( byteBuf instanceof FileByteBuff )
        {
            return ( (FileByteBuff) byteBuf ).readAllBytes();
        }
        byte[] bytes = new byte[ byteBuf.readableBytes() ];
        byteBuf.readBytes( bytes, 0, byteBuf.readableBytes() );
        return bytes;
    }

    @Override
    public String asString()
    {
        if( byteBuf == null )
        {
            return Strings.EMPTY;
        }
        return byteBuf.toString( UTF_8 );
    }

    @Override
    public String asString( Charset charset )
    {
        if( byteBuf == null )
        {
            return Strings.EMPTY;
        }
        return byteBuf.toString( charset );
    }
}
