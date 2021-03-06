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
package io.werval.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;

import static io.werval.util.Charsets.UTF_8;
import static io.werval.util.IllegalArguments.ensureNotNull;

/**
 * ByteSource backed by a byte[].
 */
public final class ByteArrayByteSource
    implements ByteSource, Serializable
{
    public static ByteArrayByteSource of( String string )
    {
        return of( string, UTF_8 );
    }

    public static ByteArrayByteSource of( String string, Charset charset )
    {
        return new ByteArrayByteSource( string.getBytes( charset ) );
    }

    private final byte[] bytes;

    /**
     * Create a new ByteSource backed by a {@literal byte[]}.
     *
     * @param bytes Array of bytes
     */
    public ByteArrayByteSource( byte[] bytes )
    {
        ensureNotNull( "Array of bytes", bytes );
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
