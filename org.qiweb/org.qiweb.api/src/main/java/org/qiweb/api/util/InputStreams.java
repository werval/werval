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
package org.qiweb.api.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream utilities.
 */
public final class InputStreams
{
    public static byte[] readAllBytes( InputStream input, int bufsize )
    {
        try
        {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[ bufsize ];
            while( ( nRead = input.read( data, 0, data.length ) ) != -1 )
            {
                buffer.write( data, 0, nRead );
            }
            buffer.flush();
            return buffer.toByteArray();
        }
        catch( IOException ex )
        {
            throw new RuntimeException( ex.getMessage(), ex );
        }
    }

    private InputStreams()
    {
    }
}
