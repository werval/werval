package org.qiweb.runtime;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public class FuckRuntime
{

    public static void main( String[] args )
    {
        URL[] cp = ( (URLClassLoader) Thread.currentThread().getContextClassLoader() ).getURLs();
        System.out.println( Arrays.toString( cp ) );
    }
}
