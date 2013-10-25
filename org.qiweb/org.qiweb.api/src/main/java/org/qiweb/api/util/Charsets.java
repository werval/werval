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
package org.qiweb.api.util;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Utilities to work with charsets.
 */
public class Charsets
{

    /**
     * 8-bit UTF (UCS Transformation Format)
     */
    public static final Charset UTF_8 = Charset.forName( "UTF-8" );
    /**
     * ISO Latin Alphabet No. 1, as known as <tt>ISO-LATIN-1</tt>
     */
    public static final Charset ISO_8859_1 = Charset.forName( "ISO-8859-1" );
    /**
     * 7-bit ASCII, as known as ISO646-US or the Basic Latin block of the
     * Unicode character set
     */
    public static final Charset US_ASCII = Charset.forName( "US-ASCII" );
    private static final ThreadLocal<Map<Charset, CharsetEncoder>> encoders = new ThreadLocal<Map<Charset, CharsetEncoder>>()
    {
        @Override
        protected Map<Charset, CharsetEncoder> initialValue()
        {
            return new IdentityHashMap<>( 4 );
        }
    };
    private static final ThreadLocal<Map<Charset, CharsetDecoder>> decoders = new ThreadLocal<Map<Charset, CharsetDecoder>>()
    {
        @Override
        protected Map<Charset, CharsetDecoder> initialValue()
        {
            return new IdentityHashMap<>( 4 );
        }
    };

    /**
     * Returns a cached thread-local {@link CharsetEncoder} for the specified <tt>charset</tt>.
     * @param charset Character encoding
     * @return  Character encoder
     */
    public static CharsetEncoder getEncoder( Charset charset )
    {
        if( charset == null )
        {
            throw new NullPointerException( "charset" );
        }

        Map<Charset, CharsetEncoder> map = encoders.get();
        CharsetEncoder encoder = map.get( charset );
        if( encoder != null )
        {
            encoder.reset();
            encoder.onMalformedInput( CodingErrorAction.REPLACE );
            encoder.onUnmappableCharacter( CodingErrorAction.REPLACE );
            return encoder;
        }

        encoder = charset.newEncoder();
        encoder.onMalformedInput( CodingErrorAction.REPLACE );
        encoder.onUnmappableCharacter( CodingErrorAction.REPLACE );
        map.put( charset, encoder );
        return encoder;
    }

    /**
     * Returns a cached thread-local {@link CharsetDecoder} for the specified <tt>charset</tt>.
     * @param charset Character encoding
     * @return  Character decoder
     */
    public static CharsetDecoder getDecoder( Charset charset )
    {
        if( charset == null )
        {
            throw new NullPointerException( "charset" );
        }

        Map<Charset, CharsetDecoder> map = decoders.get();
        CharsetDecoder decoder = map.get( charset );
        if( decoder != null )
        {
            decoder.reset();
            decoder.onMalformedInput( CodingErrorAction.REPLACE );
            decoder.onUnmappableCharacter( CodingErrorAction.REPLACE );
            return decoder;
        }

        decoder = charset.newDecoder();
        decoder.onMalformedInput( CodingErrorAction.REPLACE );
        decoder.onUnmappableCharacter( CodingErrorAction.REPLACE );
        map.put( charset, decoder );
        return decoder;
    }

    private Charsets()
    {
    }

}
