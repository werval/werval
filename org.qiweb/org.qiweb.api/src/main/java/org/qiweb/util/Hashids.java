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
package org.qiweb.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.qiweb.util.IllegalArguments.ensureNotEmpty;
import static org.qiweb.util.Strings.EMPTY;
import static org.qiweb.util.Strings.SPACE;
import static org.qiweb.util.Strings.isEmpty;

/**
 * Generate short unique string secret identities from positive numbers.
 * <p>
 * See <a href="http://hashids.org/">hashids.org</a>.
 * This is a Java port of the <a href="https://github.com/ivanakimov/hashids.js">javascript version</a>.
 * <p>
 * The Hashids's {@literal salt} is used as a secret to generate unique strings using a given {@literal alphabet}.
 * Generated strings can have a {@literal minimumLength}.
 * <p>
 * If you use this to obfuscates identities, do not expose your {@literal salt}, {@literal alphabet} nor
 * {@literal separators} to a client, client-side is not safe.
 * <p>
 * Only positive numbers are supported.
 * All methods in this class with throw an {@link IllegalArgumentException} if a negative number is given.
 * If you want to use negative numbers you'll have to handle prepending {@literal -} to the hash string yourself and
 * would be limited to single number hashes.
 * <p>
 * Here is sample code to handle negative numbers prepending {@literal -} to them:
 * <pre>
 * Hashids hashids = new Hashids( "this is your salt" );
 * long number = -1234567890;
 * String enc = ( Math.abs( number ) != number ? "-" : "" ) + hashids.encode( Math.abs( number ) );
 * long dec = enc.startsWith( "-" ) ? -hashids.decode( enc.substring( 1 ) )[0] : hashids.decode( enc )[0];
 * </pre>
 * Note that this isn't true Hashids anymore and that this is limited to single number hashes.
 */
public final class Hashids
{
    /**
     * Default alphabet.
     * <p>
     * {@literal abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890}
     */
    public static final String DEFAULT_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    /**
     * Default separators.
     * <p>
     * Used to prevent the generation of strings that contains rude english words.
     * <p>
     * {@literal cfhistuCFHISTU}
     */
    public static final String DEFAULT_SEPARATORS = "cfhistuCFHISTU";

    /**
     * Maximum number value.
     * <p>
     * This is {@literal 9_007_199_254_740_991}, or {@literal 2^53-1}, or {@literal Number.MAX_VALUE-1} in Javascript.
     * This limit is mandatory in order to ensure interoperability.
     */
    public static final long MAX_NUMBER_VALUE = 9_007_199_254_740_992L - 1;

    /**
     * Hashids Builder.
     * <p>
     * Immutable, reusable builder.
     * Each method return a new builder instance.
     * <p>
     * Defaults are no salt, {@link #DEFAULT_ALPHABET}, no minimum length and {@link #DEFAULT_SEPARATORS}.
     */
    public static final class Builder
    {
        private final String salt;
        private final String alphabet;
        private final String separators;
        private final int minimumLength;

        /**
         * Create a new Hashids builder.
         */
        public Builder()
        {
            this.salt = EMPTY;
            this.alphabet = DEFAULT_ALPHABET;
            this.separators = DEFAULT_SEPARATORS;
            this.minimumLength = 0;
        }

        private Builder( String salt, String alphabet, String separators, int minimumLength )
        {
            this.salt = salt;
            this.alphabet = alphabet;
            this.separators = separators;
            this.minimumLength = minimumLength;
        }

        public Builder salt( String salt )
        {
            return new Builder( salt, alphabet, separators, minimumLength );
        }

        public Builder alphabet( String alphabet )
        {
            return new Builder( salt, alphabet, separators, minimumLength );
        }

        public Builder separators( String separators )
        {
            return new Builder( salt, alphabet, separators, minimumLength );
        }

        public Builder minimumLength( int minimumLength )
        {
            return new Builder( salt, alphabet, separators, minimumLength );
        }

        public Hashids build()
        {
            return new Hashids( salt, minimumLength, alphabet, separators );
        }
    }

    private static final int MIN_ALPHABET_LENGTH = 16;
    private static final double SEP_DIV = 3.5;
    private static final int GUARD_DIV = 12;

    private final String salt;
    private final int minimumLength;
    private final String alphabet;
    private final String separators;
    private final String guards;

    public Hashids( String salt )
    {
        this( salt, 0 );
    }

    public Hashids( String salt, int minimumLength )
    {
        this( salt, minimumLength, DEFAULT_ALPHABET );
    }

    public Hashids( String salt, String alphabet )
    {
        this( salt, 0, alphabet );
    }

    public Hashids( String salt, int minimumLength, String alphabet )
    {
        this( salt, minimumLength, alphabet, DEFAULT_SEPARATORS );
    }

    public Hashids( String salt, int minimumLength, String alphabet, String separators )
    {
        ensureNotEmpty( "alphabet", alphabet );

        this.salt = salt == null ? EMPTY : salt;
        this.minimumLength = minimumLength < 0 ? 0 : minimumLength;

        String uniqueAlphabet = EMPTY;
        for( int idx = 0; idx < alphabet.length(); idx++ )
        {
            if( !uniqueAlphabet.contains( EMPTY + alphabet.charAt( idx ) ) )
            {
                uniqueAlphabet += EMPTY + alphabet.charAt( idx );
            }
        }
        alphabet = uniqueAlphabet;

        if( alphabet.length() < MIN_ALPHABET_LENGTH )
        {
            throw new IllegalArgumentException(
                "Alphabet must contain at least " + MIN_ALPHABET_LENGTH + " unique characters"
            );
        }

        if( alphabet.contains( SPACE ) )
        {
            throw new IllegalArgumentException( "Alphabet cannot contains spaces" );
        }

        // separators should contain only characters present in alphabet;
        // alphabet should not contains separators
        String seps = separators == null ? EMPTY : separators;
        for( int sepIdx = 0; sepIdx < seps.length(); sepIdx++ )
        {
            int alphaIdx = alphabet.indexOf( seps.charAt( sepIdx ) );
            if( alphaIdx == -1 )
            {
                seps = seps.substring( 0, sepIdx ) + SPACE + seps.substring( sepIdx + 1 );
            }
            else
            {
                alphabet = alphabet.substring( 0, alphaIdx ) + SPACE + alphabet.substring( alphaIdx + 1 );
            }
        }

        alphabet = alphabet.replaceAll( "\\s+", EMPTY );
        seps = seps.replaceAll( "\\s+", EMPTY );
        seps = consistentShuffle( seps, this.salt );

        if( isEmpty( seps ) || ( alphabet.length() / seps.length() ) > SEP_DIV )
        {
            int sepsLen = (int) Math.ceil( alphabet.length() / SEP_DIV );
            if( sepsLen == 1 )
            {
                sepsLen++;
            }
            if( sepsLen > seps.length() )
            {
                int diff = sepsLen - seps.length();
                seps += alphabet.substring( 0, diff );
                alphabet = alphabet.substring( diff );
            }
            else
            {
                seps = seps.substring( 0, sepsLen );
            }
        }

        alphabet = consistentShuffle( alphabet, this.salt );
        // round up using double cast
        // int guardCount = (int) Math.ceil( (double) ( alphabet.length() / GUARD_DIV ) );
        int guardCount = (int) Math.ceil( alphabet.length() / GUARD_DIV );

        if( alphabet.length() < 3 )
        {
            guards = seps.substring( 0, guardCount );
            seps = seps.substring( guardCount );
        }
        else
        {
            guards = alphabet.substring( 0, guardCount );
            alphabet = alphabet.substring( guardCount );
        }

        this.alphabet = alphabet;
        this.separators = seps;
    }

    /**
     * Encrypt numbers to string.
     *
     * @param numbers The numbers to encrypt
     *
     * @return The encrypted string
     */
    public String encode( long... numbers )
    {
        if( numbers.length == 0 )
        {
            return EMPTY;
        }
        return doEncode( numbers );
    }

    /**
     * Encrypt numbers to string.
     *
     * @param numbers The numbers to encrypt
     *
     * @return The encrypted string
     */
    public String encode( int... numbers )
    {
        if( numbers.length == 0 )
        {
            return EMPTY;
        }
        long[] longs = new long[ numbers.length ];
        for( int idx = 0; idx < numbers.length; idx++ )
        {
            longs[idx] = numbers[idx];

        }
        return doEncode( longs );
    }

    /**
     * Decrypt string to numbers.
     *
     * @param hash The encrypted string
     *
     * @return The decrypted numbers
     */
    public long[] decode( String hash )
    {
        if( isEmpty( hash ) )
        {
            return new long[ 0 ];
        }
        return doDecode( hash, alphabet );
    }

    /**
     * Decrypt string to integers.
     *
     * @param hash The encrypted string
     *
     * @return The decrypted integers
     *
     * @throws IllegalArgumentException if decoded number is out of integer range, shouldn't you be using longs instead?
     */
    public int[] decodeInts( String hash )
    {
        if( isEmpty( hash ) )
        {
            return new int[ 0 ];
        }
        long[] numbers = doDecode( hash, alphabet );
        int[] ints = new int[ numbers.length ];
        for( int idx = 0; idx < numbers.length; idx++ )
        {
            long number = numbers[idx];
            if( number < Integer.MIN_VALUE || number > Integer.MAX_VALUE )
            {
                throw new IllegalArgumentException( "Number out of range" );
            }
            ints[idx] = (int) number;
        }
        return ints;
    }

    /**
     * Encrypt hexa to string.
     *
     * @param hexa The hexa to encrypt
     *
     * @return The encrypted string
     */
    public String encodeHex( String hexa )
    {
        if( !hexa.matches( "^[0-9a-fA-F]+$" ) )
        {
            return EMPTY;
        }
        Matcher matcher = Pattern.compile( "[\\w\\W]{1,12}" ).matcher( hexa );
        List<Long> matched = new ArrayList<>();
        while( matcher.find() )
        {
            matched.add( Long.parseLong( "1" + matcher.group(), 16 ) );
        }
        return doEncode( toArray( matched ) );
    }

    /**
     * Decrypt string to numbers as hex.
     *
     * @param hash The encrypted string
     *
     * @return The decrypted numbers as hex
     */
    public String decodeHex( String hash )
    {
        StringBuilder sb = new StringBuilder();
        long[] numbers = decode( hash );
        for( long number : numbers )
        {
            sb.append( Long.toHexString( number ).substring( 1 ) );
        }
        return sb.toString();
    }

    private String doEncode( long... numbers )
    {
        int numberHashInt = 0;
        for( int idx = 0; idx < numbers.length; idx++ )
        {
            if( numbers[idx] < 0 || numbers[idx] > MAX_NUMBER_VALUE )
            {
                throw new IllegalArgumentException( "Number out of range" );
            }
            numberHashInt += numbers[idx] % ( idx + 100 );
        }
        String decodeAlphabet = alphabet;
        final char lotery = decodeAlphabet.toCharArray()[numberHashInt % decodeAlphabet.length()];

        String result = lotery + EMPTY;

        String buffer;
        int sepsIdx, guardIdx;
        for( int idx = 0; idx < numbers.length; idx++ )
        {
            long num = numbers[idx];
            buffer = lotery + salt + decodeAlphabet;

            decodeAlphabet = consistentShuffle( decodeAlphabet, buffer.substring( 0, decodeAlphabet.length() ) );
            final String last = hash( num, decodeAlphabet );

            result += last;

            if( idx + 1 < numbers.length )
            {
                num %= ( (int) last.toCharArray()[0] + idx );
                sepsIdx = (int) ( num % separators.length() );
                result += separators.toCharArray()[sepsIdx];
            }
        }

        if( result.length() < minimumLength )
        {
            guardIdx = ( numberHashInt + (int) ( result.toCharArray()[0] ) ) % guards.length();
            char guard = guards.toCharArray()[guardIdx];

            result = guard + result;

            if( result.length() < minimumLength )
            {
                guardIdx = ( numberHashInt + (int) ( result.toCharArray()[2] ) ) % guards.length();
                guard = guards.toCharArray()[guardIdx];

                result += guard;
            }
        }

        final int halfLen = decodeAlphabet.length() / 2;
        while( result.length() < minimumLength )
        {
            decodeAlphabet = consistentShuffle( decodeAlphabet, decodeAlphabet );
            result = decodeAlphabet.substring( halfLen ) + result + decodeAlphabet.substring( 0, halfLen );
            final int excess = result.length() - minimumLength;
            if( excess > 0 )
            {
                int startPos = excess / 2;
                result = result.substring( startPos, startPos + minimumLength );
            }
        }

        return result;
    }

    private long[] doDecode( String hash, String alphabet )
    {
        int idx = 0;
        String[] hashArray = hash.replaceAll( "[" + guards + "]", SPACE ).split( SPACE );
        if( hashArray.length == 3 || hashArray.length == 2 )
        {
            idx = 1;
        }
        String hashBreakdown = hashArray[idx];

        final char lottery = hashBreakdown.toCharArray()[0];
        hashBreakdown = hashBreakdown.substring( 1 );
        hashBreakdown = hashBreakdown.replaceAll( "[" + separators + "]", SPACE );
        hashArray = hashBreakdown.split( SPACE );

        final List<Long> result = new ArrayList<>();

        String buffer;
        for( String subHash : hashArray )
        {
            buffer = lottery + salt + alphabet;
            alphabet = consistentShuffle( alphabet, buffer.substring( 0, alphabet.length() ) );
            result.add( unhash( subHash, alphabet ) );
        }
        long[] resultArray = toArray( result );
        if( !doEncode( resultArray ).equals( hash ) )
        {
            resultArray = new long[ 0 ];
        }
        return resultArray;
    }

    private String consistentShuffle( String alphabet, String salt )
    {
        if( salt.length() <= 0 )
        {
            return alphabet;
        }
        final char[] saltChars = salt.toCharArray();
        int ascVal, j;
        char tmp;
        for( int idx = alphabet.length() - 1, v = 0, p = 0; idx > 0; idx--, v++ )
        {
            v %= salt.length();
            ascVal = (int) saltChars[v];
            p += ascVal;
            j = ( ascVal + v + p ) % idx;

            tmp = alphabet.charAt( j );
            alphabet = alphabet.substring( 0, j ) + alphabet.charAt( idx ) + alphabet.substring( j + 1 );
            alphabet = alphabet.substring( 0, idx ) + tmp + alphabet.substring( idx + 1 );
        }
        return alphabet;
    }

    private String hash( long input, String alphabet )
    {
        String hash = EMPTY;
        final int alphabetLen = alphabet.length();
        final char[] alphabetChars = alphabet.toCharArray();
        do
        {
            hash = alphabetChars[(int) ( input % alphabetLen )] + hash;
            input /= alphabetLen;
        }
        while( input > 0 );
        return hash;
    }

    private Long unhash( String input, String alphabet )
    {
        long number = 0;
        long pos;
        final char[] inputChars = input.toCharArray();
        for( int idx = 0; idx < input.length(); idx++ )
        {
            pos = alphabet.indexOf( inputChars[idx] );
            number += pos * Math.pow( alphabet.length(), input.length() - idx - 1 );
        }
        return number;
    }

    private long[] toArray( List<Long> longs )
    {
        final long[] result = new long[ longs.size() ];
        int idx = 0;
        for( Long aLong : longs )
        {
            result[idx++] = aLong;
        }
        return result;
    }
}
