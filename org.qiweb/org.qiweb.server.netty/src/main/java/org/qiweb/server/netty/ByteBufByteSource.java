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
package org.qiweb.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.qiweb.api.util.ByteSource;
import org.qiweb.api.util.InputStreams;

/**
 * ByteSource backed by a Netty ByteBuf.
 */
public class ByteBufByteSource
    implements ByteSource
{
    private final ByteBuf bytebuf;

    public ByteBufByteSource( ByteBuf bytebuf )
    {
        this.bytebuf = bytebuf;
    }

    @Override
    public byte[] asBytes()
    {
        return InputStreams.readAllBytes( asStream(), bytebuf.capacity() );
    }

    @Override
    public InputStream asStream()
    {
        return new ByteBufInputStream( bytebuf );
    }

    @Override
    public String asString( Charset charset )
    {
        return new String( asBytes(), charset );
    }
}
