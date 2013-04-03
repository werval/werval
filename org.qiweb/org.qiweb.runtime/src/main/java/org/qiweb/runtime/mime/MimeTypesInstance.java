package org.qiweb.runtime.mime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import org.qiweb.api.mime.MimeTypes;
import org.qiweb.runtime.exceptions.QiWebRuntimeException;

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
        return ofFilename( file.getName() );
    }

    @Override
    public String ofPath( String path )
    {
        return ofFile( new File( path ) );
    }

    @Override
    public String ofFilename( String filename )
    {
        int lastDotIndex = filename.lastIndexOf( '.' );
        if( lastDotIndex > 0 )
        {
            return ofExtension( filename.substring( lastDotIndex + 1 ) );
        }
        return null;
    }

    @Override
    public String ofExtension( String extension )
    {
        return extToType.getProperty( extension );
    }

    @Override
    public boolean isTextual( String mimetype )
    {
        if( mimetype.startsWith( "text/" ) || mimetype.startsWith( "application/json" ) )
        {
            return true;
        }
        return false;
    }
}
