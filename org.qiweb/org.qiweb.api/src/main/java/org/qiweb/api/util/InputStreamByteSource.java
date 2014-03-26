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
package org.qiweb.api.util;

import java.io.InputStream;
import java.nio.charset.Charset;
import org.qiweb.api.exceptions.IllegalArguments;

/**
 * ByteSource backed by an InputStream.
 */
public final class InputStreamByteSource
    implements ByteSource
{
    private final InputStream input;
    private final int bufsize;

    /**
     * Create a new ByteSource backed by a InputStream.
     *
     * @param input   InputStream
     * @param bufsize Size of the buffer used when consuming the InputStream
     */
    public InputStreamByteSource( InputStream input, int bufsize )
    {
        IllegalArguments.ensureNotNull( "InputStream", input );
        this.input = input;
        this.bufsize = bufsize;
    }

    @Override
    public byte[] asBytes()
    {
        return InputStreams.readAllBytes( input, bufsize );
    }

    @Override
    public InputStream asStream()
    {
        return input;
    }

    @Override
    public String asString( Charset charset )
    {
        return new String( asBytes(), charset );
    }
}
