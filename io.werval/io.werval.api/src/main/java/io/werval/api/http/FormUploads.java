/*
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
package io.werval.api.http;

import io.werval.util.MultiValued;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;

/**
 * Form Uploads.
 *
 * @navcomposed 1 - * Upload
 */
public interface FormUploads
    extends MultiValued<String, FormUploads.Upload>
{
    /**
     * Form Upload.
     */
    interface Upload
    {
        /**
         * @return Upload's content-type, optional
         */
        Optional<String> contentType();

        /**
         * @return Upload's charset, optional
         */
        Optional<Charset> charset();

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
         * @return Upload data as a String using the upload character encoding if defined, the default character
         *         encoding otherwise.
         */
        String asString();

        /**
         * @param charset Charset to use
         *
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
