package org.qiweb.devshell;

/* package */ final class Color
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

    public static void normal( String message )
    {
        output( message, "" );
    }

    public static void black( String message )
    {
        output( message, ANSI_BLACK );
    }

    public static void red( String message )
    {
        output( message, ANSI_RED );
    }

    public static void green( String message )
    {
        output( message, ANSI_GREEN );
    }

    public static void yellow( String message )
    {
        output( message, ANSI_YELLOW );
    }

    public static void blue( String message )
    {
        output( message, ANSI_BLUE );
    }

    public static void purple( String message )
    {
        output( message, ANSI_PURPLE );
    }

    public static void cyan( String message )
    {
        output( message, ANSI_CYAN );
    }

    public static void white( String message )
    {
        output( message, ANSI_WHITE );
    }

    private static void output( String message, String color )
    {
        System.out.println( ANSI_RESET + color + message + ANSI_RESET );
    }

    private Color()
    {
    }
}
