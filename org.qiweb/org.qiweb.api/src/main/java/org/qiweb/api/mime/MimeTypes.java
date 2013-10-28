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
import java.nio.charset.Charset;

/**
 * MimeTypes registry.
 * 
 * <p>Mime Type lookups always succeed and thus never return null.</p>
 * <p>If unable to detect, {@link #APPLICATION_OCTET_STREAM} is returned.</p>
 * <p>The known Mime Types are extracted from Apache HTTP Server source code by the QiWeb Build System.</p>
 * <p><img src="doc-files/apache-httpd.jpg" alt="Apache HTTP Server"></p>
 * <p>
 *     You can add Mime Type definitions in <code>application.conf</code> at the
 *     <code>qiweb.mimetypes.supplementary</code> config property.
 * </p>
 * <p>Here is an example defining two more Mime Types:</p>
 * <pre>
 * qiweb.mimetypes.supplementary: {
 *     foo: "application/vnd.acme.foo",
 *     bar: "application/vnd.acme.bar"
 * }
 * </pre>
 * <p>
 *     Files with a <code>foo</code> or <code>bar</code> extension will then be respectively served with a
 *     <code>application/vnd.acme.foo</code> or <code>application/vnd.acme.bar</code> Mime Type.
 * </p>
 * <p><strong>Textual mimetypes and character encoding</strong></p>
 * <p>
 *     Adding character encoding extensions to a mime type is a frequent task.
 *     The MimeTypes API provide helpers for this matter.
 *     You can ask if a given mime type is textual.
 *     You can ask the default charset to use for a given textual mimetype.
 * </p>
 * <p>
 *     As a shortcut, methods named <code>of*WithCharset</code> add a charset if the mimetype is textual using the default QiWeb character encoding or an override from the textuals mimetypes definition in configuration.
 * </p>
 * <p>
 *     Here is an example registering the two mimetypes used above as textual while defining their default character encoding:
 * </p>
 * <pre>
 * qiweb.character-encoding: utf-8
 * qiweb.mimetypes.textuals: {
 *      "application/vnd.acme.foo": default,
 *      "application/vnd.acme.bar": koi8-ru
 * }
 * </pre>
 */
public interface MimeTypes
    extends MimeTypesNames
{

    /**
     * @param file File
     * @return MimeType of a File
     */
    String ofFile( File file );

    /**
     * @param file File
     * @return MimeType of a File with optional character encoding if textual
     */
    String ofFileWithCharset( File file );

    /**
     * @param file File
     * @param charset Character encoding
     * @return MimeType of a File with character encoding, textual or not
     */
    String ofFileWithCharset( File file, Charset charset );

    /**
     * @param path Path
     * @return MimeType of a path
     */
    String ofPath( String path );

    /**
     * @param path Path
     * @return MimeType of a Path with optional character encoding if textual
     */
    String ofPathWithCharset( String path );

    /**
     * @param path Path
     * @param charset Character encoding
     * @return MimeType of a Path with character encoding, textual or not
     */
    String ofPathWithCharset( String path, Charset charset );

    /**
     * @param filename File name
     * @return MimeType of a filename
     */
    String ofFilename( String filename );

    /**
     * @param filename File name
     * @return MimeType of a filename with optional character encoding if textual
     */
    String ofFilenameWithCharset( String filename );

    /**
     * @param filename File name
     * @param charset Character encoding
     * @return MimeType of a filename with character encoding, textual or not
     */
    String ofFilenameWithCharset( String filename, Charset charset );

    /**
     * @param extension File extension
     * @return MimeType of a file extension
     */
    String ofExtension( String extension );

    /**
     * @param extension File extension
     * @return MimeType of a Extension with optional character encoding if textual
     */
    String ofExtensionWithCharset( String extension );

    /**
     * @param extension File extension
     * @param charset Character encoding
     * @return MimeType of a Extension with character encoding, textual or not
     */
    String ofExtensionWithCharset( String extension, Charset charset );

    /**
     * @param mimetype Mimetype
     * @return TRUE is mimetype is textual, otherwise return FALSE
     */
    boolean isTextual( String mimetype );

    /**
     * @param mimetype Mimetype
     * @return Character encoding to use for the given MimeType
     * @throws IllegalArgumentException when mimetype is not textual
     */
    Charset charsetOfTextual( String mimetype );

    /**
     * @param mimetype Mimetype
     * @param charset Character encoding
     * @return MimeType with character encoding information set
     */
    String withCharset( String mimetype, Charset charset );

    /**
     * @param mimetype Mimetype
     * @return MimeType with character encoding information set
     * @throws IllegalArgumentException when mimetype is not textual
     */
    String withCharsetOfTextual( String mimetype );

}
