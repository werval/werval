package org.qiweb.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
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

    public Outcome file( String file )
    {
        ensureNotEmpty( "File", file );
        return file( new File( file ) );
    }

    public Outcome file( Path file )
    {
        ensureNotNull( "File", file );
        return file( file.toFile() );
    }

    public Outcome file( File file )
    {
        ensureNotNull( "File", file );
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
            LOG.trace( "Will serve '{}' with mimetype '{}'", file, mimetype );
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

    public Outcome tree( String root, String path )
    {
        ensureNotEmpty( "Root", root );
        return tree( new File( root ), path );
    }

    public Outcome tree( Path root, String path )
    {
        ensureNotNull( "Root", root );
        return tree( root.toFile(), path );
    }

    public Outcome tree( File root, String path )
    {
        ensureNotNull( "Root", root );
        ensureNotEmpty( "Path", path );
        if( !root.isDirectory() )
        {
            LOG.warn( "Root '{}' is not a directory, will return Not Found.", root );
            return outcomes().notFound().build();
        }
        return file( new File( root, path ) );
    }
}
