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

import java.math.BigInteger;

/**
 * Numbers utilities.
 */
public final class Numbers
{
    /**
     * Safely convert a {@literal long} to an {@literal int}.
     *
     * If the {@literal long} is greater than {@link Integer#MAX_VALUE} then {@link Integer#MAX_VALUE} is returned.
     * <p>
     * If the {@literal long} is lesser than {@link Integer#MIN_VALUE} then {@link Integer#MIN_VALUE} is returned.
     * <p>
     * Implementation use {@literal BigInteger}s. See {@link Math#toIntExact(long)} for a throwing equivalent.
     *
     * @param aLong A {@literal long}
     *
     * @return Converted {@literal int}
     */
    public static int safeIntValueOf( long aLong )
    {
        return safeIntValueOf( BigInteger.valueOf( aLong ) );
    }

    /**
     * Safely compute the sum of the given {@literal long}s.
     *
     * If the sum of all the {@literal long}s is greater than {@link Long#MAX_VALUE} then {@link Long#MAX_VALUE} is
     * returned.
     * <p>
     * If the sum of all the {@literal long}s is lesser than {@link Long#MIN_VALUE} then {@link Long#MIN_VALUE} is
     * returned.
     * <p>
     * Implementation use {@literal BigInteger}s. See {@link Math#addExact(long, long)} for a throwing equivalent.
     *
     * @param longs {@literal long}s to sum
     *
     * @return Computed sum
     */
    public static long safeLongValueOfSum( long... longs )
    {
        BigInteger bigint = BigInteger.ZERO;
        for( long each : longs )
        {
            bigint = bigint.add( BigInteger.valueOf( each ) );
        }
        return safeLongValueOf( bigint );
    }

    /**
     * Safely compute the product of the given {@literal long}s.
     *
     * If the product of all the {@literal long}s is greater than {@link Long#MAX_VALUE} then {@link Long#MAX_VALUE} is
     * returned.
     * <p>
     * If the product of all the {@literal long}s is lesser than {@link Long#MIN_VALUE} then {@link Long#MIN_VALUE} is
     * returned.
     * <p>
     * Implementation use {@literal BigInteger}s. See {@link Math#multiplyExact(long, long)} for a throwing equivalent.
     *
     * @param longs {@literal long}s to multiply
     *
     * @return Computed product
     */
    public static long safeLongValueOfMultiply( long... longs )
    {
        BigInteger bigint = BigInteger.ONE;
        for( long each : longs )
        {
            bigint = bigint.multiply( BigInteger.valueOf( each ) );
        }
        return safeLongValueOf( bigint );
    }

    private static int safeIntValueOf( BigInteger bigint )
    {
        try
        {
            return bigint.intValueExact();
        }
        catch( ArithmeticException tooBIG )
        {
            return bigint.signum() == 1 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }
    }

    private static long safeLongValueOf( BigInteger bigint )
    {
        try
        {
            return bigint.longValueExact();
        }
        catch( ArithmeticException tooBIG )
        {
            return bigint.signum() == 1 ? Long.MAX_VALUE : Long.MIN_VALUE;
        }
    }

    private Numbers()
    {
    }
}
