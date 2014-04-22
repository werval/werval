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
     *
     * @param aLong A {@literal long}
     *
     * @return Converted {@literal int}
     */
    public static int safeIntValueOf( long aLong )
    {
        try
        {
            return BigInteger.valueOf( aLong ).intValueExact();
        }
        catch( ArithmeticException tooBIG )
        {
            return aLong > 0 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }
    }

    /**
     * Safely compute the sum of the given {@literal long}s.
     *
     * If the sum of all the {@literal long}s is greater than {@link Long#MAX_VALUE} then {@link Long#MAX_VALUE} is
     * returned.
     * <p>
     * If the sum of all the {@literal long}s is lesser than {@link Long#MIN_VALUE} then {@link Long#MIN_VALUE} is
     * returned.
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
