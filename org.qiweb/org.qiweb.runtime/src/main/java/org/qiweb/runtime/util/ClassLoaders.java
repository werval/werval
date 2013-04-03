package org.qiweb.runtime.util;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.codeartisans.java.toolbox.Strings;
import org.qiweb.api.exceptions.QiWebException;

public final class ClassLoaders
{

    private static final String TAB = "    ";

    public static void printURLs( ClassLoader classLoader )
    {
        printURLs( classLoader, new PrintWriter( new OutputStreamWriter( System.out,
                                                                         Charset.forName( "UTF-8" ) ) ) );
    }

    public static void printURLs( ClassLoader classLoader, PrintWriter output )
    {
        if( !( classLoader instanceof URLClassLoader ) )
        {
            throw new IllegalArgumentException( "ClassLoader is not an instance of URLClassLoader" );
        }
        try
        {
            URLClassLoader urlLoader = (URLClassLoader) classLoader;
            int idx = 0;
            while( urlLoader != null )
            {
                String indent = indents( idx ) + "+ ";
                output.println( indent + urlLoader );
                for( URL url : urlLoader.getURLs() )
                {
                    output.println( indent + TAB + url );
                }
                ClassLoader parent = urlLoader.getParent();
                if( parent == null )
                {
                    // break
                    urlLoader = null;
                }
                else if( parent instanceof URLClassLoader )
                {
                    urlLoader = (URLClassLoader) parent;
                }
                else
                {
                    output.println( indent + TAB + "Not URLClassLoader parent: " + parent );
                    // break
                    urlLoader = null;
                }
                idx++;
            }
            output.flush();
        }
        catch( Exception ex )
        {
            throw new QiWebException( "Unable to print URLs from ClassLoaders hierarchy: " + ex.getMessage(), ex );
        }
    }

    public static void printLoadedClasses( ClassLoader classLoader )
    {
        printLoadedClasses( classLoader, new PrintWriter( new OutputStreamWriter( System.out,
                                                                                  Charset.forName( "UTF-8" ) ) ) );
    }

    @SuppressWarnings(
        {
        "unchecked",
        "UseOfObsoleteCollectionType"
    } )
    public static void printLoadedClasses( ClassLoader classLoader, PrintWriter output )
    {
        try
        {
            Field clClassesField = ClassLoader.class.getDeclaredField( "classes" );
            clClassesField.setAccessible( true );
            ClassLoader cl = classLoader;
            int idx = 0;
            while( cl != null )
            {
                String indent = indents( idx ) + "+ ";
                output.println( indent + cl );
                java.util.Vector<Class<?>> classes = (java.util.Vector<Class<?>>) clClassesField.get( cl );
                List<String> classNames = new ArrayList<>();
                for( Class<?> clazz : classes )
                {
                    classNames.add( clazz.getName() );
                }
                Collections.sort( classNames );
                for( String className : classNames )
                {
                    output.println( indent + TAB + className );
                }
                cl = cl.getParent();
                idx++;
            }
            clClassesField.setAccessible( false );
            output.flush();
        }
        catch( NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex )
        {
            throw new QiWebException( "Unable to print loaded classes from ClassLoaders hierarchy: " + ex.getMessage(), ex );
        }
    }

    private static String indents( int count )
    {
        String[] indents = new String[ count ];
        Arrays.fill( indents, TAB );
        return Strings.join( indents );
    }

    private ClassLoaders()
    {
    }
}
