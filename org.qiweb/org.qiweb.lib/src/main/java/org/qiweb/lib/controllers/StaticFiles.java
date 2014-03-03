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
package org.qiweb.lib.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import org.qiweb.api.Mode;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.api.util.Dates;
import org.qiweb.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Locale.US;
import static org.qiweb.api.context.CurrentContext.application;
import static org.qiweb.api.context.CurrentContext.outcomes;
import static org.qiweb.api.context.CurrentContext.request;
import static org.qiweb.api.context.CurrentContext.response;
import static org.qiweb.api.exceptions.NullArgumentException.ensureNotEmpty;
import static org.qiweb.api.exceptions.NullArgumentException.ensureNotNull;
import static org.qiweb.api.http.Headers.Names.CACHE_CONTROL;
import static org.qiweb.api.http.Headers.Names.CONTENT_TYPE;
import static org.qiweb.api.http.Headers.Names.ETAG;
import static org.qiweb.api.http.Headers.Names.IF_MODIFIED_SINCE;
import static org.qiweb.api.http.Headers.Names.IF_NONE_MATCH;
import static org.qiweb.api.http.Headers.Names.LAST_MODIFIED;
import static org.qiweb.api.mime.MimeTypesNames.APPLICATION_OCTET_STREAM;
import static org.qiweb.api.util.Charsets.US_ASCII;

/**
 * Controller to serve static files or directory tree.
 * <p>Cache behaviour can be tweeked with <code>qiweb.lib.staticfiles</code> config properties.</p>
 * <p>Always use streamed identity transfer encoding.</p>
 * <p>MimeType detection done using Application MimeTypes, fallback to <code>application/octet-stream</code>.</p>
 * <p>Log 404 at DEBUG level.</p>
 * <p>Log 200 at TRACE level.</p>
 * <p><strong>Keep in mind that not all deployment strategies will be compatible with the use of this controller.</strong></p>
 */
// TODO Add Range request support in StaticFiles
public class StaticFiles
{

    private static final Logger LOG = LoggerFactory.getLogger( StaticFiles.class );

    /**
     * Serve a filesystem directory as read-only resources.
     * <p>
     *     If a directory is requested, filenames set in the <code>qiweb.lib.staticfiles.index</code> config property is
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
     *     If a directory is requested, filenames set in the <code>qiweb.lib.staticfiles.index</code> config property is
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
     *     If a directory is requested, filenames set in the <code>qiweb.lib.staticfiles.index</code> config property is
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
            List<String> indexFileNames = application().config().stringList( "qiweb.lib.staticfiles.index" );
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
                withBody( "You just attempted a directory traversal attack, did you?" ).
                build();
        }
        if( !file.isFile() )
        {
            LOG.debug( "Requested file '{}' not found", file );
            return outcomes().notFound().build();
        }

        // Cache-Control
        if( application().mode() == Mode.DEV )
        {
            response().headers().with( CACHE_CONTROL, "no-cache" );
        }
        else
        {
            Long maxAge = application().config().seconds( "qiweb.lib.staticfiles.cache.maxage" );
            if( maxAge.equals( 0L ) )
            {
                response().headers().with( CACHE_CONTROL, "no-cache" );
            }
            else
            {
                response().headers().with( CACHE_CONTROL, "max-age=" + maxAge );
            }
        }
        // ETag
        long lastModified = file.lastModified();
        final String etag = "\"" + lastModified + "-" + file.hashCode() + "\"";
        if( application().config().bool( "qiweb.lib.staticfiles.cache.etag" ) )
        {
            response().headers().with( ETAG, etag );
        }
        // If-None-Match, If-Modified-Since & Last-Modified
        boolean notModified = false;
        if( request().headers().names().contains( IF_NONE_MATCH ) )
        {
            notModified = request().headers().singleValue( IF_NONE_MATCH ).equals( etag );
        }
        if( request().headers().names().contains( IF_MODIFIED_SINCE ) )
        {
            String ifModifiedSince = request().headers().singleValue( IF_MODIFIED_SINCE );
            if( !Strings.isEmpty( ifModifiedSince ) )
            {
                try
                {
                    if( Dates.HTTP.parse( ifModifiedSince ).getTime() >= lastModified )
                    {
                        notModified = true;
                    }
                }
                catch( ParseException ex )
                {
                    LOG.warn( "Unable to parse HTTP date: " + ifModifiedSince, ex );
                }
            }
        }

        // 304 Not-Modified or 200 Last-Modified
        if( notModified )
        {
            return outcomes().notModified().build();
        }
        response().headers().with( LAST_MODIFIED, Dates.HTTP.format( new Date( lastModified ) ) );

        // MimeType
        String mimetype = application().mimeTypes().ofFileWithCharset( file );
        response().headers().with( CONTENT_TYPE, mimetype );

        // Disposition and filename
        String filename = US_ASCII.newEncoder().canEncode( file.getName() )
                          ? "; filename=\"" + file.getName() + "\""
                          : "; filename*=" + application().defaultCharset().name().toLowerCase( US ) + "; filename=\"" + file.getName() + "\"";
        if( APPLICATION_OCTET_STREAM.equals( mimetype ) )
        {
            // Browser will prompt the user, we should provide a filename
            response().headers().with( "Content-Disposition", "attachment" + filename );
        }
        else
        {
            // Tell the browser to attempt to display the file and provide a filename in case it cannot
            response().headers().with( "Content-Disposition", "inline" + filename );
        }

        // Service
        try
        {
            LOG.trace( "Outcome will stream '{}' as '{}'", file, mimetype );
            return outcomes().
                ok().
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
