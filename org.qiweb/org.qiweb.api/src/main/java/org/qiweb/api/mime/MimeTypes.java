package org.qiweb.api.mime;

import java.io.File;

public interface MimeTypes
{

    String ofFile( File file );

    String ofPath( String path );

    String ofFilename( String filename );

    String ofExtension( String extension );

    boolean isTextual( String mimetype );
}
