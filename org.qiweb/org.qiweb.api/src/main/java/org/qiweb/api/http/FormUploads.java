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
package org.qiweb.api.http;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Form Uploads.
 */
public interface FormUploads
{
    /**
     * @return TRUE if there's no upload, FALSE otherwise
     */
    boolean isEmpty();

    /**
     * @param name Name of the upload
     * @return TRUE if there's an upload with the given name
     */
    boolean has( String name );

    /**
     * @return  All upload names as immutable Set&lt;String&gt;.
     */
    Set<String> names();

    /**
     * Get single form upload value, ensuring it has only one value.
     *
     * @param name Name of the form upload
     * @return  Value for this form upload name or an empty String
     * @throws IllegalStateException if there is multiple values for this form upload
     */
    Upload singleValue( String name );

    /**
     * Get first form upload value.
     *
     * @param name Name of the form upload
     * @return  First value for this form upload name or an empty String
     */
    Upload firstValue( String name );

    /**
     * Get last form upload value.
     *
     * @param name Name of the form upload
     * @return  Last value for this form upload name or an empty String
     */
    Upload lastValue( String name );

    /**
     * Get all form upload values.
     *
     * @param name Name of the form upload
     * @return  All String values of the form for the given name as immutable List&lt;String&gt;,
     *          or an immutable empty one.
     */
    List<Upload> values( String name );

    /**
     * Get all form uploads single values, ensuring each has only one value.
     *
     * @return  Every single value of each form upload as immutable Map&lt;String,String&gt;, or an empty
     *          immutable one.
     * @throws IllegalStateException if there is multiple values for a parameter
     */
    Map<String, Upload> singleValues();

    /**
     * Get all form uploads first values.
     *
     * @return  Every first value of each form upload as immutable Map&lt;String,String&gt;, or an empty
     *          immutable one.
     */
    Map<String, Upload> firstValues();

    /**
     * Get all form uploads last values.
     *
     * @return  Every last value of each form upload as immutable Map&lt;String,String&gt;, or an empty
     *          immutable one.
     */
    Map<String, Upload> lastValues();

    /**
     * Get all form uploads values.
     *
     * @return  Every values of each form upload as immutable Map&lt;String,List&lt;String&gt;&gt;,
     *          or an empty immutable one.
     */
    Map<String, List<Upload>> allValues();

    /**
     * Form Upload.
     */
    interface Upload
    {
        /**
         * @return Upload's content-type or null if not defined
         */
        String contentType();

        /**
         * @return Upload's charset or null if not defined
         */
        Charset charset();

        /**
         * @return Upload's filename
         */
        String filename();

        /**
         * @return Upload's length
         */
        long length();

        /**
         * @return InputStream to the upload data
         */
        InputStream asStream();

        /**
         * @return Upload data bytes
         */
        byte[] asBytes();

        /**
         * @return  Upload data as a String using the upload character encoding if defined, the QiWeb default character
         *          encoding otherwise.
         */
        String asString();

        /**
         * @param charset Charset to use
         * @return Upload data as a String
         */
        String asString( Charset charset );

        /**
         * Move upload data to a File.
         * 
         * @param destination Destination file
         */
        void moveTo( File destination );
    }
}
