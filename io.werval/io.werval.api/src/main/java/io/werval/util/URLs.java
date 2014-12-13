/*
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
package io.werval.util;

import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * URL related utility methods.
 */
public final class URLs
{
    /**
     * Translates a string into application/x-www-form-urlencoded format using UTF-8 character encoding.
     *
     * @param string  String to be translated
     * @param charset Character encoding
     *
     * @return the translated String
     */
    public static String encode( String string, Charset charset )
    {
        try
        {
            return URLEncoder.encode( string, charset.name() );
        }
        catch( UnsupportedEncodingException ex )
        {
            throw new UncheckedIOException( "Unable to URL encode " + string, ex );
        }
    }

    /**
     * Decodes a application/x-www-form-urlencoded string.
     *
     * @param string  the String to decode
     * @param charset Character encoding
     *
     * @return the newly decoded String
     */
    public static String decode( String string, Charset charset )
    {
        try
        {
            return URLDecoder.decode( string, charset.name() );
        }
        catch( UnsupportedEncodingException ex )
        {
            throw new UncheckedIOException( "Unable to URL decode " + string, ex );
        }
    }

    /**
     * Append query string to a URL.
     *
     * @param url         The URL
     * @param queryString The query string data
     * @param charset     Character encoding
     *
     * @return The URL with query string data appended
     */
    public static String appendQueryString( final String url, Map<String, List<String>> queryString, Charset charset )
    {
        int hashIdx = url.indexOf( '#' );
        StringBuilder builder = new StringBuilder( hashIdx > 0 ? url.substring( 0, hashIdx ) : url );
        if( queryString != null && !queryString.isEmpty() )
        {
            Iterator<Entry<String, List<String>>> itKey = queryString.entrySet().iterator();
            if( itKey.hasNext() )
            {
                builder.append( url.contains( "?" ) ? '&' : '?' );
                while( itKey.hasNext() )
                {
                    Entry<String, List<String>> entry = itKey.next();
                    String paramName = entry.getKey();
                    for( Iterator<String> itVal = entry.getValue().iterator(); itVal.hasNext(); )
                    {
                        String paramValue = itVal.next();
                        builder.
                            append( encode( paramName, charset ) ).
                            append( "=" ).
                            append( encode( paramValue, charset ) ).
                            append( itVal.hasNext() ? "&" : "" );
                    }
                    builder.append( itKey.hasNext() ? "&" : "" );
                }
            }
        }
        if( hashIdx > 0 )
        {
            builder.append( url.substring( hashIdx ) );
        }
        return builder.toString();
    }

    private URLs()
    {
    }
}
