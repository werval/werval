/*
 * Copyright (c) 2013-2014 the original author or authors
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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.qiweb.util.Charsets;

/**
 * URI Query String.
 */
public interface QueryString
{
    /**
     * @return TRUE if there's no query string parameter, FALSE otherwise
     */
    boolean isEmpty();

    /**
     * @param name Name of the query string parameter
     *
     * @return TRUE if there's a query string parameter with the given name
     */
    boolean has( String name );

    /**
     * @return All query string parameter names as immutable Set&lt;String&gt;.
     */
    Set<String> names();

    /**
     * Get single query parameter value, ensuring it has only one value.
     *
     * @param name Name of the query string parameter
     *
     * @return Value for this query string parameter name or an empty String
     *
     * @throws IllegalStateException if there is multiple values for this query string parameter
     */
    String singleValue( String name );

    /**
     * Get first query string parameter value.
     *
     * @param name Name of the query string parameter
     *
     * @return First value for this query string parameter name or an empty String
     */
    String firstValue( String name );

    /**
     * Get last query string parameter value.
     *
     * @param name Name of the query string parameter
     *
     * @return Last value for this query string parameter name or an empty String
     */
    String lastValue( String name );

    /**
     * Get all query string parameter values.
     *
     * @param name Name of the query string parameter
     *
     * @return All String values from the query string for the given name as immutable List&lt;String&gt;,
     *         or an immutable empty one.
     */
    List<String> values( String name );

    /**
     * Get all query string parameters single values, ensuring each has only one value.
     *
     * @return Every single value of each query string parameter as immutable Map&lt;String,String&gt;, or an empty
     *         immutable one.
     *
     * @throws IllegalStateException if there is multiple values for a parameter
     */
    Map<String, String> singleValues();

    /**
     * Get all query string parameters first values.
     *
     * @return Every first value of each query string parameter as immutable Map&lt;String,String&gt;, or an empty
     *         immutable one.
     */
    Map<String, String> firstValues();

    /**
     * Get all query string parameters last values.
     *
     * @return Every last value of each query string parameter as immutable Map&lt;String,String&gt;, or an empty
     *         immutable one.
     */
    Map<String, String> lastValues();

    /**
     * Get all query string parameters values.
     *
     * @return Every values of each query string parameter as immutable Map&lt;String,List&lt;String&gt;&gt;,
     *         or an empty immutable one.
     */
    Map<String, List<String>> allValues();

    /**
     * Creates an URL-encoded URI from a path string and key-value parameter pairs.
     *
     * This encoder is for one time use only. Create a new instance for each URI.
     * <pre>
     * QueryStringCodec.Encoder encoder = new QueryStringCodec.Encoder("/hello");
     * encoder.addParam("recipient", "world");
     * assert encoder.toString().equals("/hello?recipient=world");
     * </pre>
     *
     * @see Decoder
     */
    class Encoder
    {
        private final Charset charset;
        private final String uri;
        private final List<Param> params = new ArrayList<>();

        /**
         * Creates a new encoder that encodes a URI that starts with the specified path string.
         *
         * The encoder will encode the URI in UTF-8.
         *
         * @param uri URI
         */
        public Encoder( String uri )
        {
            this( uri, Charsets.UTF_8 );
        }

        /**
         * Creates a new encoder that encodes a URI that starts with the specified path string in the specified charset.
         *
         * @param uri     URI
         * @param charset Charset
         */
        public Encoder( String uri, Charset charset )
        {
            if( uri == null )
            {
                throw new NullPointerException( "getUri" );
            }
            if( charset == null )
            {
                throw new NullPointerException( "charset" );
            }

            this.uri = uri;
            this.charset = charset;
        }

        /**
         * Adds a parameter with the specified name and value to this encoder.
         *
         * @param name  Name
         * @param value Value
         */
        public void addParam( String name, String value )
        {
            if( name == null )
            {
                throw new NullPointerException( "name" );
            }
            if( value == null )
            {
                throw new NullPointerException( "value" );
            }
            params.add( new Param( name, value ) );
        }

        /**
         * Returns the URL-encoded URI object which was created from the path string
         * specified in the constructor and the parameters added by
         * {@link #addParam(String, String)} getMethod.
         *
         * @return URI
         *
         * @throws java.net.URISyntaxException when the URI is invalid
         */
        public URI toUri()
            throws URISyntaxException
        {
            return new URI( toString() );
        }

        /**
         * Returns the URL-encoded URI which was created from the path string
         * specified in the constructor and the parameters added by
         * {@link #addParam(String, String)} getMethod.
         *
         * @return URI
         */
        @Override
        public String toString()
        {
            if( params.isEmpty() )
            {
                return uri;
            }
            else
            {
                StringBuilder sb = new StringBuilder( uri ).append( '?' );
                for( int i = 0; i < params.size(); i++ )
                {
                    Param param = params.get( i );
                    sb.append( encodeComponent( param.name, charset ) );
                    sb.append( '=' );
                    sb.append( encodeComponent( param.value, charset ) );
                    if( i != params.size() - 1 )
                    {
                        sb.append( '&' );
                    }
                }
                return sb.toString();
            }
        }

        private static String encodeComponent( String s, Charset charset )
        {
            // TODO: Optimize me.
            try
            {
                return URLEncoder.encode( s, charset.name() ).replace( "+", "%20" );
            }
            catch( UnsupportedEncodingException e )
            {
                throw new UnsupportedCharsetException( charset.name() );
            }
        }

        private static final class Param
        {
            private final String name;
            private final String value;

            Param( String name, String value )
            {
                this.value = value;
                this.name = name;
            }
        }
    }

    /**
     * Splits an HTTP query string into a path string and key-value parameter pairs.
     *
     * This decoder is for one time use only. Create a new instance for each URI:
     * <pre>
     * QueryStringCodec.Decoder decoder = new QueryStringCodec.Decoder("/hello?recipient=world&amp;x=1;y=2");
     * assert decoder.getPath().equals("/hello");
     * assert decoder.getParameters().get("recipient").get(0).equals("world");
     * assert decoder.getParameters().get("x").get(0).equals("1");
     * assert decoder.getParameters().get("y").get(0).equals("2");
     * </pre>
     *
     * This decoder can also decode the content of an HTTP POST request whose
     * content type is <tt>application/x-www-form-urlencoded</tt>:
     * <pre>
     * QueryStringCodec.Decoder decoder = new QueryStringCodec.Decoder("recipient=world&amp;x=1;y=2", false);
     * ...
     * </pre>
     *
     * <h3>HashDOS vulnerability fix</h3>
     *
     * As a workaround to the <a href="http://goo.gl/I4Nky">HashDOS</a> vulnerability, the decoder
     * limits the maximum number of decoded key-value parameter pairs, up to {@literal 1024} by
     * default, and you can configure it when you construct the decoder by passing an additional
     * integer parameter.
     *
     * @see Encoder
     */
    class Decoder
    {
        private static final int DEFAULT_MAX_PARAMS = 1024;

        private final Charset charset;
        private final String uri;
        private final boolean hasPath;
        private final int maxParams;
        private String path;
        private Map<String, List<String>> params;
        private int nParams;

        /**
         * Creates a new decoder that decodes the specified URI.
         *
         * The decoder will assume that the query string is encoded in UTF-8.
         *
         * @param uri URI
         */
        public Decoder( String uri )
        {
            this( uri, Charsets.UTF_8 );
        }

        /**
         * Creates a new decoder that decodes the specified URI encoded in the specified charset.
         *
         * @param uri     URI
         * @param hasPath Has path?
         */
        public Decoder( String uri, boolean hasPath )
        {
            this( uri, Charsets.UTF_8, hasPath );
        }

        /**
         * Creates a new decoder that decodes the specified URI encoded in the specified charset.
         *
         * @param uri     URI
         * @param charset Charset
         */
        public Decoder( String uri, Charset charset )
        {
            this( uri, charset, true );
        }

        /**
         * Creates a new decoder that decodes the specified URI encoded in the specified charset.
         *
         * @param uri     URI
         * @param charset Charset
         * @param hasPath Has path?
         */
        public Decoder( String uri, Charset charset, boolean hasPath )
        {
            this( uri, charset, hasPath, DEFAULT_MAX_PARAMS );
        }

        /**
         * Creates a new decoder that decodes the specified URI encoded in the specified charset.
         *
         * @param uri       URI
         * @param charset   Charset
         * @param hasPath   Has path?
         * @param maxParams Maximum parameters count
         */
        public Decoder( String uri, Charset charset, boolean hasPath, int maxParams )
        {
            if( uri == null )
            {
                throw new NullPointerException( "getUri" );
            }
            if( charset == null )
            {
                throw new NullPointerException( "charset" );
            }
            if( maxParams <= 0 )
            {
                throw new IllegalArgumentException(
                    "maxParams: " + maxParams + " (expected: a positive integer)" );
            }

            this.uri = uri;
            this.charset = charset;
            this.maxParams = maxParams;
            this.hasPath = hasPath;
        }

        /**
         * Creates a new decoder that decodes the specified URI.
         *
         * The decoder will assume that the query string is encoded in UTF-8.
         *
         * @param uri URI
         */
        public Decoder( URI uri )
        {
            this( uri, Charsets.UTF_8 );
        }

        /**
         * Creates a new decoder that decodes the specified URI encoded in the specified charset.
         *
         * @param uri     URI
         * @param charset Charset
         */
        public Decoder( URI uri, Charset charset )
        {
            this( uri, charset, DEFAULT_MAX_PARAMS );
        }

        /**
         * Creates a new decoder that decodes the specified URI encoded in the specified charset.
         *
         * @param uri       URI
         * @param charset   Charset
         * @param maxParams Maximum parameters count
         */
        public Decoder( URI uri, Charset charset, int maxParams )
        {
            if( uri == null )
            {
                throw new NullPointerException( "getUri" );
            }
            if( charset == null )
            {
                throw new NullPointerException( "charset" );
            }
            if( maxParams <= 0 )
            {
                throw new IllegalArgumentException(
                    "maxParams: " + maxParams + " (expected: a positive integer)" );
            }

            String rawPath = uri.getRawPath();
            if( rawPath != null )
            {
                hasPath = true;
            }
            else
            {
                rawPath = "";
                hasPath = false;
            }
            // Also take care of cut of things like "http://localhost"
            this.uri = rawPath + '?' + uri.getRawQuery();

            this.charset = charset;
            this.maxParams = maxParams;
        }

        /**
         * Returns the decoded path string of the URI.
         *
         * @return Path
         */
        public String path()
        {
            if( path == null )
            {
                if( !hasPath )
                {
                    path = "";
                    return path;
                }

                int pathEndPos = uri.indexOf( '?' );
                if( pathEndPos < 0 )
                {
                    path = uri;
                }
                else
                {
                    path = uri.substring( 0, pathEndPos );
                    return path;
                }
            }
            return path;
        }

        /**
         * Returns the decoded key-value parameter pairs of the URI.
         *
         * @return Parameters
         */
        public Map<String, List<String>> parameters()
        {
            if( params == null )
            {
                if( hasPath )
                {
                    int pathLength = path().length();
                    if( uri.length() == pathLength )
                    {
                        return Collections.emptyMap();
                    }
                    decodeParams( uri.substring( pathLength + 1 ) );
                }
                else
                {
                    if( uri.isEmpty() )
                    {
                        return Collections.emptyMap();
                    }
                    decodeParams( uri );
                }
            }
            return params;
        }

        private void decodeParams( String s )
        {
            params = new LinkedHashMap<>();
            nParams = 0;
            String name = null;
            int pos = 0; // Beginning of the unprocessed region
            int i;       // End of the unprocessed region
            char c;  // Current character
            for( i = 0; i < s.length(); i++ )
            {
                c = s.charAt( i );
                if( c == '=' && name == null )
                {
                    if( pos != i )
                    {
                        name = decodeComponent( s.substring( pos, i ), charset );
                    }
                    pos = i + 1;
                    // http://www.w3.org/TR/html401/appendix/notes.html#h-B.2.2
                }
                else if( c == '&' || c == ';' )
                {
                    if( name == null && pos != i )
                    {
                        // We haven't seen an `=' so far but moved forward.
                        // Must be a param of the form '&a&' so add it with
                        // an empty value.
                        if( !addParam( params, decodeComponent( s.substring( pos, i ), charset ), "" ) )
                        {
                            return;
                        }
                    }
                    else if( name != null )
                    {
                        if( !addParam( params, name, decodeComponent( s.substring( pos, i ), charset ) ) )
                        {
                            return;
                        }
                        name = null;
                    }
                    pos = i + 1;
                }
            }

            if( pos != i )
            {  // Are there characters we haven't dealt with?
                if( name == null )
                {     // Yes and we haven't seen any `='.
                    addParam( params, decodeComponent( s.substring( pos, i ), charset ), "" );
                }
                else
                {                // Yes and this must be the last value.
                    addParam( params, name, decodeComponent( s.substring( pos, i ), charset ) );
                }
            }
            else if( name != null )
            {  // Have we seen a name without value?
                addParam( params, name, "" );
            }
        }

        private boolean addParam( Map<String, List<String>> params, String name, String value )
        {
            if( nParams >= maxParams )
            {
                return false;
            }

            List<String> values = params.get( name );
            if( values == null )
            {
                values = new ArrayList<>( 1 );  // Often there's only 1 value.
                params.put( name, values );
            }
            values.add( value );
            nParams++;
            return true;
        }

        /**
         * Decodes a bit of an URL encoded by a browser.
         *
         * This is equivalent to calling {@link #decodeComponent(String, Charset)}
         * with the UTF-8 charset (recommended to comply with RFC 3986, Section 2).
         *
         * @param s The string to decode (can be empty).
         *
         * @return The decoded string, or {@code s} if there's nothing to decode.
         *         If the string to decode is {@code null}, returns an empty string.
         *
         * @throws IllegalArgumentException if the string contains a malformed
         *                                  escape sequence.
         */
        public static String decodeComponent( final String s )
        {
            return decodeComponent( s, Charsets.UTF_8 );
        }

        /**
         * Decodes a bit of an URL encoded by a browser.
         *
         * The string is expected to be encoded as per RFC 3986, Section 2.
         * This is the encoding used by JavaScript functions {@code encodeURI}
         * and {@code encodeURIComponent}, but not {@code escape}. For example
         * in this encoding, &eacute; (in Unicode {@code U+00E9} or in UTF-8
         * {@code 0xC3 0xA9}) is encoded as {@code %C3%A9} or {@code %c3%a9}.
         * <p>
         * This is essentially equivalent to calling
         * URLDecoder#decode(String, String) URLDecoder.decode(s, charset.name())}
         * except that it's over 2x faster and generates less garbage for the GC.
         * Actually this function doesn't allocate any memory if there's nothing
         * to decode, the argument itself is returned.
         *
         * @param s       The string to decode (can be empty).
         * @param charset The charset to use to decode the string (should really
         *                be {@link Charsets#UTF_8}.
         *
         * @return The decoded string, or {@code s} if there's nothing to decode.
         *         If the string to decode is {@code null}, returns an empty string.
         *
         * @throws IllegalArgumentException if the string contains a malformed
         *                                  escape sequence.
         */
        @SuppressWarnings( "fallthrough" )
        public static String decodeComponent( final String s,
                                              final Charset charset )
        {
            if( s == null )
            {
                return "";
            }
            final int size = s.length();
            boolean modified = false;
            for( int i = 0; i < size; i++ )
            {
                final char c = s.charAt( i );
                if( c == '%' || c == '+' )
                {
                    modified = true;
                    break;
                }
            }
            if( !modified )
            {
                return s;
            }
            final byte[] buf = new byte[ size ];
            int pos = 0;  // position in `buf'.
            for( int i = 0; i < size; i++ )
            {
                char c = s.charAt( i );
                switch( c )
                {
                    case '+':
                        buf[pos++] = ' ';  // "+" -> " "
                        break;
                    case '%':
                        if( i == size - 1 )
                        {
                            throw new IllegalArgumentException( "unterminated escape sequence at end of string: " + s );
                        }
                        c = s.charAt( ++i );
                        if( c == '%' )
                        {
                            buf[pos++] = '%';  // "%%" -> "%"
                            break;
                        }
                        if( i == size - 1 )
                        {
                            throw new IllegalArgumentException( "partial escape sequence at end of string: " + s );
                        }
                        c = decodeHexNibble( c );
                        final char c2 = decodeHexNibble( s.charAt( ++i ) );
                        if( c == Character.MAX_VALUE || c2 == Character.MAX_VALUE )
                        {
                            throw new IllegalArgumentException(
                                "invalid escape sequence `%" + s.charAt( i - 1 )
                                + s.charAt( i ) + "' at index " + ( i - 2 )
                                + " of: " + s );
                        }
                        c = (char) ( c * 16 + c2 );
                    // Fall through.
                    default:
                        buf[pos++] = (byte) c;
                        break;
                }
            }
            return new String( buf, 0, pos, charset );
        }

        /**
         * Helper to decode half of a hexadecimal number from a string.
         *
         * @param c The ASCII character of the hexadecimal number to decode.
         *          Must be in the range {@code [0-9a-fA-F]}.
         *
         * @return The hexadecimal value represented in the ASCII character
         *         given, or {@link Character#MAX_VALUE} if the character is invalid.
         */
        private static char decodeHexNibble( final char c )
        {
            if( '0' <= c && c <= '9' )
            {
                return (char) ( c - '0' );
            }
            else if( 'a' <= c && c <= 'f' )
            {
                return (char) ( c - 'a' + 10 );
            }
            else if( 'A' <= c && c <= 'F' )
            {
                return (char) ( c - 'A' + 10 );
            }
            else
            {
                return Character.MAX_VALUE;
            }
        }
    }
}
