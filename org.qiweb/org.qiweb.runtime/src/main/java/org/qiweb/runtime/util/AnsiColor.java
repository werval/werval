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
package org.qiweb.runtime.util;

import static java.util.Locale.US;

/**
 * ANSI Colors.
 */
public final class AnsiColor
{
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    private static final boolean SUPPORTED = !System.getProperty( "os.name" ).toLowerCase( US ).contains( "windows" );

    public static String normal( String message )
    {
        return color( message, "" );
    }

    public static String black( String message )
    {
        return color( message, ANSI_BLACK );
    }

    public static String red( String message )
    {
        return color( message, ANSI_RED );
    }

    public static String green( String message )
    {
        return color( message, ANSI_GREEN );
    }

    public static String yellow( String message )
    {
        return color( message, ANSI_YELLOW );
    }

    public static String blue( String message )
    {
        return color( message, ANSI_BLUE );
    }

    public static String purple( String message )
    {
        return color( message, ANSI_PURPLE );
    }

    public static String cyan( String message )
    {
        return color( message, ANSI_CYAN );
    }

    public static String white( String message )
    {
        return color( message, ANSI_WHITE );
    }

    private static String color( String message, String color )
    {
        if( SUPPORTED )
        {
            return ANSI_RESET + color + message + ANSI_RESET;
        }
        return message;
    }

    private AnsiColor()
    {
    }
}
