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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.qiweb.api.exceptions.NullArgumentException;

/**
 * ByteSource backed by a byte[].
 */
public class ByteArrayByteSource
    extends ByteSource
{
    private final byte[] bytes;

    public ByteArrayByteSource( byte[] bytes )
    {
        NullArgumentException.ensureNotNull( "Array of bytes", bytes );
        this.bytes = bytes;
    }

    @Override
    public byte[] asBytes()
    {
        return bytes;
    }

    @Override
    public InputStream asStream()
    {
        return new ByteArrayInputStream( bytes );
    }

    @Override
    public String asString( Charset charset )
    {
        return new String( bytes, charset );
    }
}
