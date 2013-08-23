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

/**
 * Request Body.
 *
 * @composed 1 - * Upload
 */
public interface RequestBody
{

    Map<String, List<String>> formAttributes();

    Map<String, List<Upload>> formUploads();

    InputStream asStream();

    byte[] asBytes();

    String asString();

    String asString( Charset charset );

    interface Upload
    {

        String contentType();

        Charset charset();

        String filename();

        long length();

        InputStream asStream();

        byte[] asBytes();

        String asString();

        String asString( Charset charset );

        boolean renameTo( File destination );
    }
}
