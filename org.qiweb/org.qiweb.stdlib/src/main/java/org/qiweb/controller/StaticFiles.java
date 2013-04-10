package org.qiweb.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;
import org.qiweb.api.controllers.Controller;
import org.qiweb.api.controllers.Outcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.codeartisans.java.toolbox.exceptions.NullArgumentException.*;

/**
 * Controller to serve static files or directory tree.
 * <p>Always use streamed identity transfer encoding.</p>
 * <p>MimeType detection done using Application MimeTypes, fallback to <code>application/octet-stream</code>.</p>
 * <p>Log 404 at DEBUG level.</p>
 * <p>Log 200 at TRACE level.</p>
 */
// TODO Add Cache handling in StaticFiles
// TODO Add Range request support in StaticFiles
public class StaticFiles
    extends Controller
{

    private static final Logger LOG = LoggerFactory.getLogger( StaticFiles.class );

    /**
     * Serve a filesystem directory as read-only resources.
     * <p>
     *     If a directory is requested, filenames set in the <code>qiweb.stdlib.staticfiles.index</code> config property is
     *     used to find an index file. Default value is <code>index.html</code> only.
     * </p>
     * @param root Root of the file tree to serve
     * @param path Path of the requeted file, relative to root
     * @return The requested file or a 404 Outcome if not found
     */
    public Outcome tree( String root, String path )
    {
        ensureNotEmpty( "Root", root );
        return tree( new File( root ), path );
    }

    /**
     * Serve a filesystem directory as read-only resources.
     * <p>
     *     If a directory is requested, filenames set in the <code>qiweb.stdlib.staticfiles.index</code> config property is
     *     used to find an index file. Default value is <code>index.html</code> only.
     * </p>
     * @param root Root of the file tree to serve
     * @param path Path of the requeted file, relative to root
     * @return The requested file or a 404 Outcome if not found
     */
    public Outcome tree( Path root, String path )
    {
        ensureNotNull( "Root", root );
        return tree( root.toFile(), path );
    }

    /**
     * Serve a filesystem directory as read-only resources.
     * <p>
     *     If a directory is requested, filenames set in the <code>qiweb.stdlib.staticfiles.index</code> config property is
     *     used to find an index file. Default value is <code>index.html</code> only.
     * </p>
     * @param root Root of the file tree to serve
     * @param path Path of the requeted file, relative to root
     * @return The requested file or a 404 Outcome if not found
     */
    public Outcome tree( File root, String path )
    {
        ensureNotNull( "Root", root );
        ensureNotNull( "Path", path );
        if( !root.isDirectory() )
        {
            LOG.warn( "Root '{}' is not a directory, review your routes. Outcome will be 404 Not Found.", root );
            return outcomes().notFound().build();
        }
        File file = new File( root, path );
        if( file.isDirectory() )
        {
            List<String> indexFileNames = application().config().getStringList( "qiweb.stdlib.staticfiles.index" );
            for( String indexFileName : indexFileNames )
            {
                File indexFile = new File( file, indexFileName );
                if( indexFile.isFile() )
                {
                    file = indexFile;
                    break;
                }
            }
        }
        return file( file );
    }

    /**
     * Serve a single file.
     * @param file Path of the requested file
     * @return The requested file or a 404 Outcome if not found
     */
    public Outcome file( String file )
    {
        ensureNotEmpty( "File", file );
        return serveFile( new File( file ) );
    }

    /**
     * Serve a single file.
     * @param file Path of the requested file
     * @return The requested file or a 404 Outcome if not found
     */
    public Outcome file( Path file )
    {
        ensureNotNull( "File", file );
        return serveFile( file.toFile() );
    }

    /**
     * Serve a single file.
     * @param file Path of the requested file
     * @return The requested file or a 404 Outcome if not found
     */
    public Outcome file( File file )
    {
        ensureNotNull( "File", file );
        return serveFile( file );
    }

    private Outcome serveFile( File file )
    {
        if( file.getPath().contains( ".." ) )
        {
            LOG.warn( "Directory traversal attempt: '{}'", file.getPath() );
            return outcomes().
                badRequest().
                as( "text/plain" ).
                withBody( "Did you just attempted a directory traversal attack? Keep out." ).
                build();
        }
        if( !file.isFile() )
        {
            LOG.debug( "Requested file '{}' not found", file );
            return outcomes().notFound().build();
        }
        String mimetype = application().mimeTypes().ofFile( file );
        mimetype = application().mimeTypes().isTextual( mimetype )
                   ? mimetype + "; charset=utf-8"
                   : mimetype;
        try
        {
            LOG.trace( "Outcome will stream '{}' as '{}'", file, mimetype );
            return outcomes().ok().
                as( mimetype ).
                withBody( new FileInputStream( file ), file.length() ).
                build();
        }
        catch( FileNotFoundException ex )
        {
            // File removed?
            LOG.debug( "Requested file '{}' not found", file );
            return outcomes().notFound().build();
        }
    }
}
