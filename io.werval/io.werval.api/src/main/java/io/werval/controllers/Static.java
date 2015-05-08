/*
 * Copyright (c) 2013-2014 the original author or authors
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
package io.werval.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import io.werval.api.Mode;
import io.werval.api.outcomes.Outcome;
import io.werval.util.Dates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Locale.US;

import static io.werval.api.context.CurrentContext.application;
import static io.werval.api.context.CurrentContext.mimeTypes;
import static io.werval.api.context.CurrentContext.outcomes;
import static io.werval.api.context.CurrentContext.request;
import static io.werval.api.context.CurrentContext.response;
import static io.werval.api.http.Headers.Names.CACHE_CONTROL;
import static io.werval.api.http.Headers.Names.CONTENT_TYPE;
import static io.werval.api.http.Headers.Names.ETAG;
import static io.werval.api.http.Headers.Names.IF_MODIFIED_SINCE;
import static io.werval.api.http.Headers.Names.IF_NONE_MATCH;
import static io.werval.api.http.Headers.Names.LAST_MODIFIED;
import static io.werval.api.mime.MimeTypesNames.APPLICATION_OCTET_STREAM;
import static io.werval.util.Charsets.US_ASCII;
import static io.werval.util.IllegalArguments.ensureNotEmpty;
import static io.werval.util.IllegalArguments.ensureNotNull;

/**
 * Serve static files or directory trees.
 * <p>
 * Cache behaviour can be tweeked with <code>werval.controllers.static</code> config properties.
 * <p>
 * Always use streamed identity transfer encoding.
 * <p>
 * MimeType detection done using Application MimeTypes, fallback to <code>application/octet-stream</code>.
 * <p>
 * Log 404 at DEBUG level.
 * <p>
 * Log 200 at TRACE level.
 * <p>
 * <strong>Keep in mind that not all deployment strategies will be compatible with the use of this controller.</strong>
 */
public class Static
{
    private static final Logger LOG = LoggerFactory.getLogger( Static.class );

    /**
     * Serve a filesystem directory as read-only resources.
     * <p>
     * If a directory is requested, filenames set in the <code>werval.controllers.static.index</code> config property
     * are used to find an index file. Default value is <strong>no index file support</strong>.
     *
     * @param root Root of the file tree to serve
     * @param path Path of the requeted file, relative to root
     *
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
     * If a directory is requested, filenames set in the <code>werval.controllers.static.index</code> config property
     * are used to find an index file. Default value is <strong>no index file support</strong>.
     *
     * @param root Root of the file tree to serve
     * @param path Path of the requeted file, relative to root
     *
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
     * If a directory is requested, filenames set in the <code>werval.controllers.static.index</code> config property
     * are used to find an index file. Default value is <strong>no index file support</strong>.
     *
     * @param root Root of the file tree to serve
     * @param path Path of the requeted file, relative to root
     *
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
            List<String> indexFileNames = application().config().stringList( "werval.controllers.static.index" );
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
     *
     * @param file Path of the requested file
     *
     * @return The requested file or a 404 Outcome if not found
     */
    public Outcome file( String file )
    {
        ensureNotEmpty( "File", file );
        return serveFile( new File( file ) );
    }

    /**
     * Serve a single file.
     *
     * @param file Path of the requested file
     *
     * @return The requested file or a 404 Outcome if not found
     */
    public Outcome file( Path file )
    {
        ensureNotNull( "File", file );
        return serveFile( file.toFile() );
    }

    /**
     * Serve a single file.
     *
     * @param file Path of the requested file
     *
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
            Long maxAge = application().config().seconds( "werval.controllers.static.cache.maxage" );
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
        if( application().config().bool( "werval.controllers.static.cache.etag" ) )
        {
            response().headers().with( ETAG, etag );
        }
        // If-None-Match, If-Modified-Since & Last-Modified
        boolean notModified = false;
        Optional<String> ifNoneMatch = request().headers().singleValueOptional( IF_NONE_MATCH );
        if( ifNoneMatch.isPresent() )
        {
            notModified = ifNoneMatch.get().equals( etag );
        }
        Optional<String> ifModifiedSince = request().headers().singleValueOptional( IF_MODIFIED_SINCE );
        if( ifModifiedSince.isPresent() )
        {
            try
            {
                if( Dates.HTTP.parse( ifModifiedSince.get() ).getTime() >= lastModified )
                {
                    notModified = true;
                }
            }
            catch( ParseException ex )
            {
                LOG.warn( "Unable to parse HTTP date: " + ifModifiedSince.get(), ex );
            }
        }

        // 304 Not-Modified or 200 Last-Modified
        if( notModified )
        {
            return outcomes().notModified().build();
        }
        response().headers().with( LAST_MODIFIED, Dates.HTTP.format( new Date( lastModified ) ) );

        // MimeType
        String mimetype = mimeTypes().ofFileWithCharset( file );
        response().headers().with( CONTENT_TYPE, mimetype );

        // Disposition and filename
        String filename = US_ASCII.newEncoder().canEncode( file.getName() )
                          ? "; filename=\"" + file.getName() + "\""
                          : "; filename*=" + application().defaultCharset().name().toLowerCase( US )
                            + "; filename=\"" + file.getName() + "\"";
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
