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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.http.FormUploads;
import org.qiweb.runtime.exceptions.BadRequestException;
import org.qiweb.runtime.util.Comparators;

import static org.qiweb.api.exceptions.NullArgumentException.ensureNotEmpty;
import static org.qiweb.api.util.Charsets.UTF_8;

public class FormUploadsInstance
    implements FormUploads
{

    private final Map<String, List<Upload>> uploads;

    public FormUploadsInstance( boolean allowMultiValuedUploads, Map<String, List<Upload>> uploads )
    {
        this.uploads = new TreeMap<>( Comparators.LOWER_CASE );
        for( Map.Entry<String, List<Upload>> entry : uploads.entrySet() )
        {
            String name = entry.getKey();
            if( !this.uploads.containsKey( name ) )
            {
                this.uploads.put( name, new ArrayList<Upload>() );
            }
            List<Upload> values = entry.getValue();
            if( !allowMultiValuedUploads && ( !this.uploads.get( name ).isEmpty() || values.size() > 1 ) )
            {
                throw new BadRequestException( "Multi-valued uploads are not allowed" );
            }
            this.uploads.get( name ).addAll( entry.getValue() );
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
        return Collections.unmodifiableSet( uploads.keySet() );
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
            return Collections.emptyList();
        }
        return Collections.unmodifiableList( uploads.get( name ) );
    }

    @Override
    public Map<String, Upload> singleValues()
    {
        Map<String, Upload> map = new TreeMap<>( Comparators.LOWER_CASE );
        for( String name : uploads.keySet() )
        {
            map.put( name, singleValue( name ) );
        }
        return Collections.unmodifiableMap( map );
    }

    @Override
    public Map<String, Upload> firstValues()
    {
        Map<String, Upload> map = new TreeMap<>( Comparators.LOWER_CASE );
        for( String name : uploads.keySet() )
        {
            map.put( name, firstValue( name ) );
        }
        return Collections.unmodifiableMap( map );
    }

    @Override
    public Map<String, Upload> lastValues()
    {
        Map<String, Upload> map = new TreeMap<>( Comparators.LOWER_CASE );
        for( String name : uploads.keySet() )
        {
            map.put( name, lastValue( name ) );
        }
        return Collections.unmodifiableMap( map );
    }

    @Override
    public Map<String, List<Upload>> allValues()
    {
        return Collections.unmodifiableMap( uploads );
    }

    public static class UploadInstance
        implements Upload
    {

        private final String contentType;
        private final Charset charset;
        private final String filename;
        private final File temporaryFile;

        public UploadInstance( String contentType, Charset charset, String filename, File temporaryFile )
        {
            this.contentType = contentType;
            this.charset = charset;
            this.filename = filename;
            this.temporaryFile = temporaryFile;
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
            return temporaryFile.length();
        }

        @Override
        public InputStream asStream()
        {
            try
            {
                return new FileInputStream( temporaryFile );
            }
            catch( FileNotFoundException ex )
            {
                throw new QiWebException( ex.getMessage(), ex );
            }
        }

        @Override
        public byte[] asBytes()
        {
            try
            {
                return Files.readAllBytes( temporaryFile.toPath() );
            }
            catch( IOException ex )
            {
                throw new QiWebException( ex.getMessage(), ex );
            }
        }

        @Override
        public String asString()
        {
            return new String( asBytes(), UTF_8 );
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
                Files.move( temporaryFile.toPath(), destination.toPath() );
            }
            catch( IOException ex )
            {
                throw new QiWebException( ex.getMessage(), ex );
            }
        }
    }
}
