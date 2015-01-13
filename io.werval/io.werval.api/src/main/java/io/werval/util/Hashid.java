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

import java.util.Arrays;
import java.util.Objects;

/**
 * Hashid.
 * <p>
 * Immutable.
 * <p>
 * Hold both all numbers and the encoded String.
 *
 * @see Hashids
 */
public final class Hashid
{
    public static final Hashid EMPTY = new Hashid( new long[ 0 ], Strings.EMPTY );
    private final long[] longs;
    private final String hash;

    public Hashid( long[] longs, String hash )
    {
        this.longs = longs;
        this.hash = hash;
    }

    public long singleLong()
    {
        if( longs.length == 0 )
        {
            throw new IllegalStateException( String.format( "Hashid %s contains no number", hash ) );
        }
        if( longs.length != 1 )
        {
            throw new IllegalStateException(
                String.format( "Hashid %s contains more than a single long (%s)", hash, longs.length )
            );
        }
        return longs[0];
    }

    public long firstLong()
    {
        if( longs.length == 0 )
        {
            throw new IllegalStateException( String.format( "Hashid %s contains no number", hash ) );
        }
        return longs[0];
    }

    public long lastLong()
    {
        if( longs.length == 0 )
        {
            throw new IllegalStateException( String.format( "Hashid %s contains no number", hash ) );
        }
        return longs[longs.length - 1];
    }

    /**
     * All longs in this Hashid.
     *
     * @return A new array with all longs in this Hashid
     */
    public long[] longs()
    {
        long[] values = new long[ longs.length ];
        System.arraycopy( longs, 0, values, 0, longs.length );
        return values;
    }

    /**
     * Hashid string.
     *
     * @return Hashid string
     */
    @Override
    public String toString()
    {
        return hash;
    }

    @Override
    public int hashCode()
    {
        int hashCode = 7;
        hashCode = 97 * hashCode + Arrays.hashCode( this.longs );
        hashCode = 97 * hashCode + Objects.hashCode( this.hash );
        return hashCode;
    }

    @Override
    public boolean equals( Object obj )
    {
        if( this == obj )
        {
            return true;
        }
        if( obj == null )
        {
            return false;
        }
        if( getClass() != obj.getClass() )
        {
            return false;
        }
        final Hashid other = (Hashid) obj;
        if( !Arrays.equals( this.longs, other.longs ) )
        {
            return false;
        }
        return Objects.equals( this.hash, other.hash );
    }
}
