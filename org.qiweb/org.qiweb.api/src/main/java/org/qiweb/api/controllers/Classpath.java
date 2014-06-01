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
package org.qiweb.api.controllers;

import java.util.List;
import org.qiweb.api.Mode;
import org.qiweb.api.outcomes.Outcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Locale.US;
import static org.qiweb.api.context.CurrentContext.application;
import static org.qiweb.api.context.CurrentContext.mimeTypes;
import static org.qiweb.api.context.CurrentContext.outcomes;
import static org.qiweb.api.context.CurrentContext.request;
import static org.qiweb.api.context.CurrentContext.response;
import static org.qiweb.api.exceptions.IllegalArguments.ensureNotEmpty;
import static org.qiweb.api.http.Headers.Names.CACHE_CONTROL;
import static org.qiweb.api.http.Headers.Names.CONTENT_TYPE;
import static org.qiweb.api.mime.MimeTypesNames.APPLICATION_OCTET_STREAM;
import static org.qiweb.api.util.Charsets.US_ASCII;
import static org.qiweb.api.util.ClassLoaders.resourceExists;
import static org.qiweb.api.util.Strings.EMPTY;
import static org.qiweb.api.util.Strings.isEmpty;

/**
 * Classpath Resources Controller.
 *
 * Always use chunked transfer encoding.
 * <p>
 * MimeType detection done using Application MimeTypes, fallback to <code>application/octet-stream</code>.
 * <p>
 * Log 404 at DEBUG level.
 * <p>
 * Log 200 at TRACE level.
 */
public class Classpath
{
    private static final Logger LOG = LoggerFactory.getLogger( Classpath.class );
    // No need for heading slash as we ask a ClassLoader instance for Resources
    // Would have been needed if we asked a Class instance for Resources
    private static final String META_INF_RESOURCES = "META-INF/resources";

    /**
     * Serve static files from META-INF/resources in classpath.
     *
     * If a directory is requested, filenames set in the <code>qiweb.controllers.classpath.index</code> config property
     * are used to find an index file. Default value is <strong>no index file support</strong>.
     *
     * @param path Path of the requested resources, relative to META-INF/resources
     *
     * @return A Chunked Outcome if found, 404 otherwise
     */
    public Outcome metainf( String path )
    {
        return resource( META_INF_RESOURCES + '/' + removeHeadingSlash( path ) );
    }

    /**
     * Serve static resources from classpath.
     *
     * If a directory is requested, filenames set in the <code>qiweb.controllers.classpath.index</code> config property
     * are used to find an index file. Default value is <strong>no index file support</strong>.
     *
     * @param basepath Base path of the requested resources, relative to the classpath root
     * @param path     Path of the requested resources, relative to the basePath parameter
     *
     * @return A Chunked Outcome if found, 404 otherwise
     */
    public Outcome resource( String basepath, String path )
    {
        return resource( basepath + '/' + removeHeadingSlash( path ) );
    }

    /**
     * Serve static resources from classpath.
     *
     * If a directory is requested, filenames set in the <code>qiweb.controllers.classpath.index</code> config property
     * are used to find an index file. Default value is <strong>no index file support</strong>.
     *
     * @param path Path of the requested resource, relative to the classpath root
     *
     * @return A Chunked Outcome if found, 404 otherwise
     */
    public Outcome resource( String path )
    {
        ensureNotEmpty( "Path", path );
        path = removeHeadingSlash( path );
        if( path.contains( ".." ) )
        {
            LOG.warn( "Directory traversal attempt: '{}'", path );
            return outcomes().
                badRequest().
                as( "text/plain" ).
                withBody( "Did you just attempted a directory traversal attack? Keep out." ).
                build();
        }
        if( !resourceExists( application().classLoader(), path ) )
        {
            List<String> indexFileNames = application().config().stringList( "qiweb.controllers.classpath.index" );
            for( String indexFileName : indexFileNames )
            {
                String indexPath = path + "/" + indexFileName;
                if( resourceExists( application().classLoader(), indexPath ) )
                {
                    path = indexPath;
                    break;
                }
            }
        }
        if( !resourceExists( application().classLoader(), path ) )
        {
            LOG.debug( "Requested resource '{}' not found", path );
            return outcomes().
                notFound().
                as( "text/plain" ).
                withBody( request().path() + " not found" ).
                build();
        }

        // Cache-Control
        if( application().mode() == Mode.DEV )
        {
            response().headers().with( CACHE_CONTROL, "no-cache" );
        }
        else
        {
            Long maxAge = application().config().seconds( "qiweb.controllers.classpath.cache.maxage" );
            if( maxAge.equals( 0L ) )
            {
                response().headers().with( CACHE_CONTROL, "no-cache" );
            }
            else
            {
                response().headers().with( CACHE_CONTROL, "max-age=" + maxAge );
            }
        }

        // Mime Type
        String mimetype = mimeTypes().ofPathWithCharset( path );
        response().headers().with( CONTENT_TYPE, mimetype );

        // Disposition and filename
        String resourceName = path.substring( path.lastIndexOf( '/' ) + 1 );
        String filename = US_ASCII.newEncoder().canEncode( resourceName )
                          ? "; filename=\"" + resourceName + "\""
                          : "; filename*=" + application().defaultCharset().name().toLowerCase( US )
                            + "; filename=\"" + resourceName + "\"";
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

        LOG.trace( "Will serve '{}' with mimetype '{}'", path, mimetype );
        return outcomes().ok().withBody( application().classLoader().getResourceAsStream( path ) ).build();
    }

    /* package */ static String removeHeadingSlash( String str )
    {
        if( isEmpty( str ) )
        {
            return EMPTY;
        }
        String result = str;
        while( result.startsWith( "/" ) )
        {
            if( result.length() < 2 )
            {
                return EMPTY;
            }
            result = result.substring( 1 );
        }
        return result;
    }

    /* package */ static String removeTrailingSlash( String str )
    {
        if( isEmpty( str ) )
        {
            return EMPTY;
        }
        String result = str;
        while( result.endsWith( "/" ) )
        {
            if( result.length() < 2 )
            {
                return EMPTY;
            }
            result = result.substring( 0, result.length() - 1 );
        }
        return result;
    }
}
