package org.qiweb.controller;

import java.io.InputStream;
import org.qiweb.api.controllers.Controller;
import org.qiweb.api.controllers.Outcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.codeartisans.java.toolbox.exceptions.NullArgumentException.*;

/**
 * META-INF/resources Controller.
 * <p>Always use chunked transfer encoding.</p>
 * <p>MimeType detection done using Application MimeTypes, fallback to <code>application/octet-stream</code>.</p>
 * <p>Log 404 at DEBUG level.</p>
 * <p>Log 200 at TRACE level.</p>
 */
public class MetaInfResources
    extends Controller
{

    private static final Logger LOG = LoggerFactory.getLogger( MetaInfResources.class );
    // No need for heading slash as we ask a ClassLoader instance for Resources
    // Would have been needed if we asked a Class instance for Resources
    private static final String META_INF_RESOURCES = "META-INF/resources/";

    /**
     * Serve static files from META-INF/resources in classpath.
     * 
     * @param path Path of the requested resources, relative to META-INF/resources
     * @return A Chunked Outcome if found, 404 otherwise
     */
    public Outcome resource( String path )
    {
        ensureNotEmpty( "Path", path );
        if( path.contains( ".." ) )
        {
            LOG.warn( "Directory traversal attempt: '{}'", path );
            return outcomes().
                badRequest().
                as( "text/plain" ).
                withBody( "Did you just attempted a directory traversal attack? Keep out." ).
                build();
        }
        String fullPath = META_INF_RESOURCES + path;
        String mimetype = application().mimeTypes().ofPath( path );
        mimetype = application().mimeTypes().isTextual( mimetype )
                   ? mimetype + "; charset=utf-8"
                   : mimetype;
        InputStream input = application().classLoader().getResourceAsStream( fullPath );
        if( input == null )
        {
            LOG.debug( "Requested resource '{}' not found at '{}'", path, fullPath );
            return outcomes().
                notFound().
                as( "text/plain" ).
                withBody( request().path() + " not found" ).
                build();
        }
        LOG.trace( "Will serve '{}' from '{}' with mimetype '{}'", path, fullPath, mimetype );
        return outcomes().ok().as( mimetype ).withBody( input ).build();
    }
}
