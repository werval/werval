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

import java.util.List;
import io.werval.api.Mode;
import io.werval.api.outcomes.Outcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.werval.util.Charsets.US_ASCII;
import static io.werval.util.ClassLoaders.resourceExists;
import static io.werval.util.IllegalArguments.ensureNotEmpty;
import static io.werval.util.Strings.withoutHead;
import static io.werval.util.Strings.withoutTrail;
import static java.util.Locale.US;
import static io.werval.api.context.CurrentContext.application;
import static io.werval.api.context.CurrentContext.mimeTypes;
import static io.werval.api.context.CurrentContext.outcomes;
import static io.werval.api.context.CurrentContext.request;
import static io.werval.api.context.CurrentContext.response;
import static io.werval.api.http.Headers.Names.CACHE_CONTROL;
import static io.werval.api.http.Headers.Names.CONTENT_TYPE;
import static io.werval.api.mime.MimeTypesNames.APPLICATION_OCTET_STREAM;

/**
 * Serve resources from the classpath.
 * <p>
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
     * <p>
     * If a directory is requested, filenames set in the <code>werval.controllers.classpath.index</code> config property
     * are used to find an index file. Default value is <strong>no index file support</strong>.
     *
     * @param path Path of the requested resources, relative to META-INF/resources
     *
     * @return A Chunked Outcome if found, 404 otherwise
     */
    public Outcome metainf( String path )
    {
        return resource( META_INF_RESOURCES + '/' + withoutHead( path, "/" ) );
    }

    /**
     * Serve static resources from classpath.
     * <p>
     * If a directory is requested, filenames set in the <code>werval.controllers.classpath.index</code> config property
     * are used to find an index file. Default value is <strong>no index file support</strong>.
     *
     * @param basepath Base path of the requested resources, relative to the classpath root
     * @param path     Path of the requested resources, relative to the basePath parameter
     *
     * @return A Chunked Outcome if found, 404 otherwise
     */
    public Outcome resource( String basepath, String path )
    {
        return resource( withoutTrail( basepath, "/" ) + '/' + withoutHead( path, "/" ) );
    }

    /**
     * Serve static resources from classpath.
     * <p>
     * If a directory is requested, filenames set in the <code>werval.controllers.classpath.index</code> config property
     * are used to find an index file. Default value is <strong>no index file support</strong>.
     *
     * @param path Path of the requested resource, relative to the classpath root
     *
     * @return A Chunked Outcome if found, 404 otherwise
     */
    public Outcome resource( String path )
    {
        ensureNotEmpty( "Path", path );
        path = withoutHead( path, "/" );
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
            List<String> indexFileNames = application().config().stringList( "werval.controllers.classpath.index" );
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
            Long maxAge = application().config().seconds( "werval.controllers.classpath.cache.maxage" );
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
}
