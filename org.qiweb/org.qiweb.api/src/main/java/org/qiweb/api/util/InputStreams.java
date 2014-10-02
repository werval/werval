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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;

/**
 * InputStream utilities.
 */
public final class InputStreams
{
    /**
     * 4K buffer size.
     */
    public static final int BUF_SIZE_4K = 4096;

    /**
     * 8K buffer size.
     */
    public static final int BUF_SIZE_8K = 8192;

    /**
     * 16K buffer size.
     */
    public static final int BUF_SIZE_16K = 16384;

    /**
     * Read all InputStream into a byte[].
     *
     * @param input   InputStream
     * @param bufsize Size of the read buffer
     *
     * @return All InputStream bytes
     *
     * @throws UncheckedIOException if something goes wrong
     */
    public static byte[] readAllBytes( InputStream input, int bufsize )
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        transferTo( input, buffer, bufsize );
        return buffer.toByteArray();
    }

    /**
     * Read all InputStream into a String.
     *
     * @param input   InputStream
     * @param bufsize Size of the read buffer
     * @param charset Charset
     *
     * @return All InputStream as String
     *
     * @throws UncheckedIOException if something goes wrong
     */
    public static String readAllAsString( InputStream input, int bufsize, Charset charset )
    {
        return new String( readAllBytes( input, bufsize ), charset );
    }

    /**
     * Transfer an InputStream to an OutputStream.
     *
     * @param input   InputStream
     * @param output  OutputStream
     * @param bufsize Size of the read buffer
     *
     * @throws UncheckedIOException if something goes wrong
     */
    public static void transferTo( InputStream input, OutputStream output, int bufsize )
    {
        try
        {
            int nRead;
            byte[] data = new byte[ bufsize ];
            while( ( nRead = input.read( data, 0, data.length ) ) != -1 )
            {
                output.write( data, 0, nRead );
            }
            output.flush();
        }
        catch( IOException ex )
        {
            throw new UncheckedIOException( ex );
        }
    }

    private InputStreams()
    {
    }
}
