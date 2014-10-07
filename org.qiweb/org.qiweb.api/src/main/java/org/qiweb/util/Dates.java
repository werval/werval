/*
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
package org.qiweb.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utilities to work with dates.
 */
public final class Dates
{
    /**
     * @return Now
     */
    public static Date now()
    {
        return new Date();
    }

    /**
     * @return Yesterday
     */
    public static Date yesterday()
    {
        return new Date( System.currentTimeMillis() - 24 * 60 * 60 );
    }

    /**
     * @return Tomorrow
     */
    public static Date tomorrow()
    {
        return new Date( System.currentTimeMillis() + 24 * 60 * 60 );
    }

    private Dates()
    {
    }

    /**
     * Utilities to work with HTTP dates.
     */
    public static final class HTTP
    {
        private static final ThreadLocal<DateFormat> HTTP_DATE_FORMAT = new ThreadLocal<DateFormat>()
        {
            @Override
            protected DateFormat initialValue()
            {
                DateFormat dateFormat = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US );
                dateFormat.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
                dateFormat.setLenient( false );
                return dateFormat;
            }
        };

        /**
         * Format HTTP dates.
         *
         * @param date Date
         *
         * @return HTTP formatted date
         */
        public static String format( Date date )
        {
            return HTTP_DATE_FORMAT.get().format( date );
        }

        /**
         * Format HTTP dates.
         *
         * @param millis Milliseconds
         *
         * @return HTTP formatted date
         */
        public static String format( long millis )
        {
            return format( new Date( millis ) );
        }

        /**
         * Parse HTTP dates.
         *
         * @param httpFormatDateString HTTP format date String
         *
         * @return Date
         *
         * @throws ParseException when the given string is not in HTTP date format
         */
        public static Date parse( String httpFormatDateString )
            throws ParseException
        {
            return HTTP_DATE_FORMAT.get().parse( httpFormatDateString );
        }

        /**
         * @return Now HTTP formatted
         */
        public static String now()
        {
            return format( new Date() );
        }

        /**
         * @return Yesterday HTTP formatted
         */
        public static String yesterday()
        {
            return format( Dates.yesterday() );
        }

        /**
         * @return Tomorrow HTTP formatted
         */
        public static String tomorrow()
        {
            return format( Dates.tomorrow() );
        }

        private HTTP()
        {
        }
    }
}
