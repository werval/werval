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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.werval.api.exceptions.WervalException;
import io.werval.api.http.FormUploads;
import io.werval.runtime.exceptions.BadRequestException;
import io.werval.util.MultiValueMapMultiValued;
import io.werval.util.TreeMultiValueMap;

import static io.werval.runtime.util.Comparators.LOWER_CASE;

public class FormUploadsInstance
    extends MultiValueMapMultiValued<String, FormUploads.Upload>
    implements FormUploads
{
    public FormUploadsInstance( Map<String, List<Upload>> values )
    {
        super( new TreeMultiValueMap<>( LOWER_CASE ), BadRequestException.BUILDER );
        if( values != null )
        {
            values.entrySet().stream().forEach(
                val -> this.mvmap.put( val.getKey(), new ArrayList<>( val.getValue() ) )
            );
        }
    }

    public static class UploadInstance
        implements Upload
    {
        private final String contentType;
        private final Charset charset;
        private final String filename;
        private final byte[] inMemoryBytes;
        private final File temporaryFile;
        private final Charset defaultCharset;

        public UploadInstance( String contentType, Charset charset,
                               String filename, File temporaryFile,
                               Charset defaultCharset )
        {
            this.contentType = contentType;
            this.charset = charset;
            this.filename = filename;
            this.inMemoryBytes = null;
            this.temporaryFile = temporaryFile;
            this.defaultCharset = defaultCharset;
        }

        public UploadInstance( String contentType, Charset charset,
                               String filename, byte[] inMemoryBytes,
                               Charset defaultCharset )
        {
            this.contentType = contentType;
            this.charset = charset;
            this.filename = filename;
            this.inMemoryBytes = inMemoryBytes;
            this.temporaryFile = null;
            this.defaultCharset = defaultCharset;
        }

        @Override
        public Optional<String> contentType()
        {
            return Optional.ofNullable( contentType );
        }

        @Override
        public Optional<Charset> charset()
        {
            return Optional.ofNullable( charset );
        }

        @Override
        public String filename()
        {
            return filename;
        }

        @Override
        public long length()
        {
            if( inMemoryBytes != null )
            {
                return inMemoryBytes.length;
            }
            return temporaryFile.length();
        }

        @Override
        public InputStream asStream()
        {
            if( inMemoryBytes != null )
            {
                return new ByteArrayInputStream( inMemoryBytes );
            }
            try
            {
                return new FileInputStream( temporaryFile );
            }
            catch( FileNotFoundException ex )
            {
                throw new WervalException( ex.getMessage(), ex );
            }
        }

        @Override
        public byte[] asBytes()
        {
            if( inMemoryBytes != null )
            {
                return inMemoryBytes;
            }
            try
            {
                return Files.readAllBytes( temporaryFile.toPath() );
            }
            catch( IOException ex )
            {
                throw new WervalException( ex.getMessage(), ex );
            }
        }

        @Override
        public String asString()
        {
            return new String( asBytes(), charset == null ? defaultCharset : charset );
        }

        @Override
        public String asString( Charset charset )
        {
            return new String( asBytes(), charset );
        }

        @Override
        public void moveTo( File destination )
        {
            try
            {
                if( inMemoryBytes != null )
                {
                    Files.write( destination.toPath(), inMemoryBytes );
                }
                else
                {
                    Files.move( temporaryFile.toPath(), destination.toPath() );
                }
            }
            catch( IOException ex )
            {
                throw new WervalException( ex.getMessage(), ex );
            }
        }

        @Override
        public String toString()
        {
            return "{contentType: " + contentType
                   + ", charset: " + ( charset == null ? defaultCharset : charset )
                   + ", filename: " + filename
                   + ", length: " + length() + " }";
        }
    }
}
