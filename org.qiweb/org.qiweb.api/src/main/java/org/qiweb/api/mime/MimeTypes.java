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
package org.qiweb.api.mime;

import java.io.File;

/**
 * MimeTypes registry.
 * 
 * <p>Mime Type lookups always succeed and thus never return null.</p>
 * <p>If unable to detect, {@link #DEFAULT_MIME_TYPE} is returned.</p>
 * <p>The known Mime Types are extracted from Apache HTTP Server source code by the QiWeb Build System.</p>
 * <p><img src="doc-files/apache-httpd.jpg"/></p>
 * <p>
 *     You can add Mime Type definitions in <code>application.conf</code> at the <code>app.mimetypes</code> config
 *     property.
 * </p>
 * <p>Here is an example defining two more Mime Types:</p>
 * <pre>
 * app: {
 *     mimetypes = {
 *         foo: "application/vnd.acme.foo",
 *         bar: "application/vnd.acme.bar"
 *     }
 * }
 * </pre>
 * <p>
 *     Files with a <code>foo</code> or <code>bar</code> extension will then be respectively served with a
 *     <code>application/vnd.acme.foo</code> or <code>application/vnd.acme.bar</code> Mime Type.
 * </p>
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
