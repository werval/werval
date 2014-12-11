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
package org.qiweb.runtime.http;

import io.werval.api.exceptions.WervalException;
import io.werval.api.http.FormUploads;
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
import java.util.Set;
import java.util.TreeMap;
import org.qiweb.runtime.exceptions.BadRequestException;

import static io.werval.util.IllegalArguments.ensureNotEmpty;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.qiweb.runtime.util.Comparators.LOWER_CASE;

public class FormUploadsInstance
    implements FormUploads
{
    private final Map<String, List<Upload>> uploads = new TreeMap<>( LOWER_CASE );

    public FormUploadsInstance( Map<String, List<Upload>> uploads )
    {
        if( uploads != null )
        {
            uploads.entrySet().stream().forEach(
                upload -> this.uploads.put( upload.getKey(), new ArrayList<>( upload.getValue() ) )
            );
        }
    }

    @Override
    public boolean isEmpty()
    {
        return uploads.isEmpty();
    }

    @Override
    public boolean has( String name )
    {
        ensureNotEmpty( "Form Upload Name", name );
        return uploads.containsKey( name );
    }

    @Override
    public Set<String> names()
    {
        return unmodifiableSet( uploads.keySet() );
    }

    @Override
    public Upload singleValue( String name )
    {
        ensureNotEmpty( "Form Upload Name", name );
        if( !uploads.containsKey( name ) )
        {
            throw new IllegalArgumentException( "No Form Upload named '" + name + "'" );
        }
        List<Upload> values = uploads.get( name );
        if( values.size() != 1 )
        {
            throw new BadRequestException( "Form Upload " + name + " has multiple values" );
        }
        return values.get( 0 );
    }

    @Override
    public Upload firstValue( String name )
    {
        ensureNotEmpty( "Form Upload Name", name );
        if( !uploads.containsKey( name ) )
        {
            throw new IllegalArgumentException( "No Form Upload named '" + name + "'" );
        }
        return uploads.get( name ).get( 0 );
    }

    @Override
    public Upload lastValue( String name )
    {
        ensureNotEmpty( "Form Upload Name", name );
        if( !uploads.containsKey( name ) )
        {
            throw new IllegalArgumentException( "No Form Upload named '" + name + "'" );
        }
        List<Upload> values = uploads.get( name );
        return values.get( values.size() - 1 );
    }

    @Override
    public List<Upload> values( String name )
    {
        ensureNotEmpty( "Form Upload Name", name );
        if( !uploads.containsKey( name ) )
        {
            return emptyList();
        }
        return unmodifiableList( uploads.get( name ) );
    }

    @Override
    public Map<String, Upload> singleValues()
    {
        Map<String, Upload> singleValues = new TreeMap<>( LOWER_CASE );
        uploads.keySet().stream().forEach( name -> singleValues.put( name, singleValue( name ) ) );
        return unmodifiableMap( singleValues );
    }

    @Override
    public Map<String, Upload> firstValues()
    {
        Map<String, Upload> firstValues = new TreeMap<>( LOWER_CASE );
        uploads.keySet().stream().forEach( name -> firstValues.put( name, firstValue( name ) ) );
        return unmodifiableMap( firstValues );
    }

    @Override
    public Map<String, Upload> lastValues()
    {
        Map<String, Upload> lastValues = new TreeMap<>( LOWER_CASE );
        uploads.keySet().stream().forEach( name -> lastValues.put( name, lastValue( name ) ) );
        return unmodifiableMap( lastValues );
    }

    @Override
    public Map<String, List<Upload>> allValues()
    {
        return unmodifiableMap( uploads );
    }

    @Override
    public String toString()
    {
        return uploads.toString();
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
        public String contentType()
        {
            return contentType;
        }

        @Override
        public Charset charset()
        {
            return charset;
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
