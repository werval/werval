/*
 * Copyright (c) 2014 the original author or authors
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
package org.qiweb.util;

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Source of bytes.
 */
public interface ByteSource
{
    /**
     * Empty ByteSource.
     */
    ByteSource EMPTY_BYTES = new ByteArrayByteSource( new byte[ 0 ] );

    /**
     * @return Content as an array of bytes
     */
    byte[] asBytes();

    /**
     * @return An InputStream of the content
     */
    InputStream asStream();

    /**
     * @param charset Charset
     *
     * @return Content as String encoded with the given Charset
     */
    String asString( Charset charset );
}
