package org.qiweb.runtime.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utilities to work with dates.
 */
public final class Dates
{

    private static ThreadLocal<DateFormat> httpFormat = new ThreadLocal<>();

    /**
     * @return HTTP DateFormat
     */
    public static DateFormat httpFormat()
    {
        if( httpFormat.get() == null )
        {
            httpFormat.set( new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US ) );
            httpFormat.get().setTimeZone( TimeZone.getTimeZone( "GMT" ) );
        }
        return httpFormat.get();
    }

    private Dates()
    {
    }
}
