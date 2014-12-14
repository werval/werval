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

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Request Body.
 *
 * @navcomposed 1 - 1 FormAttributes
 * @navcomposed 1 - 1 FormUploads
 */
public interface RequestBody
{
    /**
     * @return Request Form Attributes
     */
    FormAttributes formAttributes();

    /**
     * @return Request Form Uploads
     */
    FormUploads formUploads();

    /**
     * @return InputStream to the body data
     */
    InputStream asStream();

    /**
     * @return Body data bytes
     */
    byte[] asBytes();

    /**
     * @return Body data as a String using the body character encoding if defined, the default character
     *         encoding otherwise.
     */
    String asString();

    /**
     * @param charset Charset to use
     *
     * @return Body data as a String
     */
    String asString( Charset charset );
}
