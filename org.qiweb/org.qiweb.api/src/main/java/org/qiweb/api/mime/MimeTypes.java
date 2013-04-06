package org.qiweb.api.mime;

import java.io.File;

/**
 * MimeTypes registry.
 * <p>Mime Type lookups always succeed and thus never return null.</p>
 * <p>If unable to detect, {@link #DEFAULT_MIME_TYPE} is returned.</p>
 */
public interface MimeTypes
{

    /**
     * Default Mime Type: application/octet-stream.
     */
    String DEFAULT_MIME_TYPE = "application/octet-stream";

    /**
     * @return MimeType of a File
     */
    String ofFile( File file );

    /**
     * @return MimeType of a path
     */
    String ofPath( String path );

    /**
     * @return MimeType of a filename
     */
    String ofFilename( String filename );

    /**
     * @return MimeType of a file extension
     */
    String ofExtension( String extension );

    /**
     * @return TRUE is mimetype is textual, otherwise return FALSE
     */
    boolean isTextual( String mimetype );
}
