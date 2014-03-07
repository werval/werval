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
package org.qiweb.api.http;

import static org.qiweb.api.exceptions.NullArgumentException.ensureNotEmpty;

public class ProtocolVersion
{
    private static final String HTTP_1_0_STRING = "HTTP/1.0";
    private static final String HTTP_1_1_STRING = "HTTP/1.1";

    /**
     * HTTP/1.0
     */
    public static final ProtocolVersion HTTP_1_0 = new ProtocolVersion( HTTP_1_0_STRING, false );

    /**
     * HTTP/1.1
     */
    public static final ProtocolVersion HTTP_1_1 = new ProtocolVersion( HTTP_1_1_STRING, true );

    public static ProtocolVersion valueOf( String text )
    {
        ensureNotEmpty( "Version text", text );
        if( HTTP_1_1_STRING.equals( text ) )
        {
            return HTTP_1_1;
        }
        if( HTTP_1_0_STRING.equals( text ) )
        {
            return HTTP_1_0;
        }
        return new ProtocolVersion( text, true );
    }

    private final String text;
    private final boolean keepAliveDefault;

    private ProtocolVersion( String text, boolean keepAliveDefault )
    {
        this.text = text;
        this.keepAliveDefault = keepAliveDefault;
    }

    public boolean isKeepAliveDefault()
    {
        return keepAliveDefault;
    }

    @Override
    public String toString()
    {
        return text;
    }
}
