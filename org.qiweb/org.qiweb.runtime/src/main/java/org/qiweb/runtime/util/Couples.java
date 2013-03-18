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
