/**
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
package org.qiweb.runtime.util;

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Source of bytes.
 */
public abstract class ByteSource
{
    public static final ByteSource EMPTY_BYTES = new ByteArrayByteSource( new byte[ 0 ] );

    public abstract byte[] asBytes();

    public abstract InputStream asStream();

    public abstract String asString( Charset charset );

    public static ByteSource wrap( byte[] bytes )
    {
        return new ByteArrayByteSource( bytes );
    }

    public static ByteSource wrap( InputStream input, int bufsize )
    {
        return new InputStreamByteSource( input, bufsize );
    }
}
