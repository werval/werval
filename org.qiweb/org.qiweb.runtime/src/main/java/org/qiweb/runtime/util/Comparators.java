package org.qiweb.runtime.util;

import java.util.Comparator;
import java.util.Locale;

public class Comparators
{

    public static final Comparator<String> LOWER_CASE = new Comparator<String>()
    {
        @Override
        public int compare( String o1, String o2 )
        {
            return o1.toLowerCase( Locale.US ).compareTo( o2.toLowerCase( Locale.US ) );
        }
    };

    private Comparators()
    {
    }
}
