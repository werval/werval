/**
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
package org.qiweb.runtime.util;

import java.util.Comparator;
import java.util.Locale;

/**
 * Comparison utilities.
 */
public final class Comparators
{
    public static final Comparator<String> LOWER_CASE = (o1, o2) -> lowerCaseCompare( o1, o2 );

    public static int lowerCaseCompare( String o1, String o2 )
    {
        return o1.toLowerCase( Locale.US ).compareTo( o2.toLowerCase( Locale.US ) );
    }

    private Comparators()
    {
    }
}
