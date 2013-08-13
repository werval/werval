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
package org.qiweb.runtime.util;

import org.codeartisans.java.toolbox.Couple;
import org.qi4j.functional.Function;

import static org.qi4j.functional.Iterables.*;

/**
 * You know, couples.
 */
public final class Couples
{

    public static final Function<Couple<?, ?>, Object> LEFT_FUNCTION = new Function<Couple<?, ?>, Object>()
    {
        @Override
        public Object map( Couple<?, ?> couple )
        {
            return couple.left();
        }
    };
    public static final Function<Couple<?, ?>, Object> RIGHT_FUNCTION = new Function<Couple<?, ?>, Object>()
    {
        @Override
        public Object map( Couple<?, ?> couple )
        {
            return couple.right();
        }
    };

    public static <T> Iterable<T> left( Iterable<?> couples )
    {
        return cast( map( LEFT_FUNCTION, couples ) );
    }

    public static <T> Iterable<T> right( Iterable<?> couples )
    {
        return cast( map( RIGHT_FUNCTION, couples ) );
    }

    private Couples()
    {
    }
}
