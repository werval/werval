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

import java.io.InputStream;
import org.qiweb.api.controllers.Outcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qiweb.api.controllers.Controller.application;
import static org.qiweb.api.controllers.Controller.outcomes;
import static org.qiweb.api.controllers.Controller.request;
import static org.qiweb.api.exceptions.NullArgumentException.ensureNotEmpty;

/**
 * Classpath Resources Controller.
 * <p>Always use chunked transfer encoding.</p>
 * <p>MimeType detection done using Application MimeTypes, fallback to <code>application/octet-stream</code>.</p>
 * <p>Log 404 at DEBUG level.</p>
 * <p>Log 200 at TRACE level.</p>
 */
public class ClasspathResources
{

    private static final Logger LOG = LoggerFactory.getLogger( ClasspathResources.class );
    // No need for heading slash as we ask a ClassLoader instance for Resources
    // Would have been needed if we asked a Class instance for Resources
    private static final String META_INF_RESOURCES = "META-INF/resources/";

    /**
     * Serve static files from META-INF/resources in classpath.
     *
     * @param path Path of the requested resources, relative to META-INF/resources
     * @return A Chunked Outcome if found, 404 otherwise
     */
    public Outcome metainf( String path )
    {
        return resource( META_INF_RESOURCES + path );
    }

    /**
     * Serve static files from resources in classpath.
     *
     * @param basepath Base path of the requested resources, relative to the classpath root
     * @param path Path of the requested resources, relative to the basePath parameter
     * @return A Chunked Outcome if found, 404 otherwise
     */
    public Outcome resource( String basepath, String path )
    {
        return resource( basepath + path );
    }

    /**
     * Serve static files from resources in classpath.
     * 
     * @param path Path of the requested resources, relative to the classpath root
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
        String mimetype = application().mimeTypes().ofPath( path );
        mimetype = application().mimeTypes().isTextual( mimetype )
                   ? mimetype + "; charset=utf-8"
                   : mimetype;
        InputStream input = application().classLoader().getResourceAsStream( path );
        if( input == null )
        {
            LOG.debug( "Requested resource '{}' not found", path );
            return outcomes().
                notFound().
                as( "text/plain" ).
                withBody( request().path() + " not found" ).
                build();
        }
        LOG.trace( "Will serve '{}' with mimetype '{}'", path, mimetype );
        return outcomes().ok().as( mimetype ).withBody( input ).build();
    }
}
