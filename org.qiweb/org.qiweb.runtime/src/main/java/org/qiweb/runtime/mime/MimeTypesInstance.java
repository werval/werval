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
package org.qiweb.runtime.mime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import org.qiweb.api.mime.MimeTypes;
import org.qiweb.runtime.exceptions.QiWebRuntimeException;

import static org.qiweb.api.exceptions.NullArgumentException.ensureNotEmpty;
import static org.qiweb.api.exceptions.NullArgumentException.ensureNotNull;

/**
 * Application MimeType instance.
 */
public class MimeTypesInstance
    implements MimeTypes
{

    private final Properties extToType = new Properties();

    public MimeTypesInstance()
    {
        try( InputStream input = getClass().getResourceAsStream( "mime-types-extensions.properties" ) )
        {
            extToType.load( input );
        }
        catch( IOException ex )
        {
            throw new QiWebRuntimeException( "Unable to load internal mime types database: " + ex.getMessage(), ex );
        }
    }

    public MimeTypesInstance( Map<String, String> applicationMimeTypes )
    {
        this();
        this.extToType.putAll( applicationMimeTypes );
    }

    @Override
    public String ofFile( File file )
    {
        ensureNotNull( "File", file );
        return ofFilename( file.getName() );
    }

    @Override
    public String ofPath( String path )
    {
        ensureNotEmpty( "Path", path );
        return ofFile( new File( path ) );
    }

    @Override
    public String ofFilename( String filename )
    {
        ensureNotEmpty( "Filename", filename );
        int lastDotIndex = filename.lastIndexOf( '.' );
        if( lastDotIndex > 0 )
        {
            return ofExtension( filename.substring( lastDotIndex + 1 ) );
        }
        return APPLICATION_OCTET_STREAM;
    }

    @Override
    public String ofExtension( String extension )
    {
        ensureNotEmpty( "Extension", extension );
        String mimeType = extToType.getProperty( extension );
        return mimeType == null ? APPLICATION_OCTET_STREAM : mimeType;
    }

    @Override
    public boolean isTextual( String mimetype )
    {
        ensureNotEmpty( "MimeType", mimetype );
        if( mimetype.startsWith( "text/" ) || mimetype.startsWith( APPLICATION_JSON ) )
        {
            return true;
        }
        return false;
    }
}
