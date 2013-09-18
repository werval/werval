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
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;
import org.qiweb.api.mime.MimeTypes;
import org.qiweb.runtime.exceptions.QiWebRuntimeException;

import static java.util.Locale.US;
import static org.qiweb.api.exceptions.NullArgumentException.ensureNotEmpty;
import static org.qiweb.api.exceptions.NullArgumentException.ensureNotNull;

/**
 * Application MimeType instance.
 */
public class MimeTypesInstance
    implements MimeTypes
{

    private final Charset defaultCharset;
    private final Properties extToType = new Properties();
    private final Map<String, Charset> textuals;

    public MimeTypesInstance( Charset defaultCharset,
                              Map<String, Charset> textuals )
    {
        this.defaultCharset = defaultCharset;
        try( InputStream input = getClass().getResourceAsStream( "mime-types-extensions.properties" ) )
        {
            extToType.load( input );
        }
        catch( IOException ex )
        {
            throw new QiWebRuntimeException( "Unable to load internal mime types database: " + ex.getMessage(), ex );
        }
        this.textuals = textuals;
    }

    public MimeTypesInstance( Charset defaultCharset,
                              Map<String, String> supplementaryMimetypes,
                              Map<String, Charset> textuals )
    {
        this( defaultCharset, textuals );
        this.extToType.putAll( supplementaryMimetypes );
    }

    @Override
    public String ofFile( File file )
    {
        ensureNotNull( "File", file );
        return ofFilename( file.getName() );
    }

    @Override
    public String ofFileWithCharset( File file )
    {
        return withCharsetIfTextual( ofFile( file ) );
    }

    @Override
    public String ofFileWithCharset( File file, Charset charset )
    {
        return withCharset( ofFile( file ), charset );
    }

    @Override
    public String ofPath( String path )
    {
        ensureNotEmpty( "Path", path );
        return ofFile( new File( path ) );
    }

    @Override
    public String ofPathWithCharset( String path )
    {
        return withCharsetIfTextual( ofPath( path ) );
    }

    @Override
    public String ofPathWithCharset( String path, Charset charset )
    {
        return withCharset( ofPath( path ), charset );
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
    public String ofFilenameWithCharset( String filename )
    {
        return withCharsetIfTextual( ofFilename( filename ) );
    }

    @Override
    public String ofFilenameWithCharset( String filename, Charset charset )
    {
        return withCharset( ofFilename( filename ), charset );
    }

    @Override
    public String ofExtension( String extension )
    {
        ensureNotEmpty( "Extension", extension );
        String mimeType = extToType.getProperty( extension );
        return mimeType == null ? APPLICATION_OCTET_STREAM : mimeType;
    }

    @Override
    public String ofExtensionWithCharset( String extension )
    {
        return withCharsetIfTextual( ofExtension( extension ) );
    }

    @Override
    public String ofExtensionWithCharset( String extension, Charset charset )
    {
        return withCharset( ofExtension( extension ), charset );
    }

    @Override
    public boolean isTextual( String mimetype )
    {
        ensureNotEmpty( "MimeType", mimetype );
        if( mimetype.startsWith( "text/" ) )
        {
            return true;
        }
        for( Map.Entry<String, Charset> textual : textuals.entrySet() )
        {
            if( mimetype.startsWith( textual.getKey() ) )
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public Charset encodingOfTextual( String mimetype )
    {
        if( !isTextual( mimetype ) )
        {
            throw new IllegalArgumentException( mimetype + " is not textual" );
        }
        Charset charset = textuals.get( mimetype );
        return charset == null ? defaultCharset : charset;
    }

    @Override
    public String withCharset( String mimetype, Charset charset )
    {
        String charsetString = charset.name().toLowerCase( US );
        return mimetype + "; charset=" + charsetString;
    }

    private String withCharsetIfTextual( String mimetype )
    {
        if( isTextual( mimetype ) )
        {
            return withCharset( mimetype, encodingOfTextual( mimetype ) );
        }
        return mimetype;
    }
}
