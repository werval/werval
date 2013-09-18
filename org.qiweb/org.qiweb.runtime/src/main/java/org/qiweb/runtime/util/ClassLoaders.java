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

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.util.Strings;

import static org.qiweb.api.util.Charsets.UTF_8;

public final class ClassLoaders
{

    private static final String TAB = "    ";

    public static List<URL> urlsOf( ClassLoader classLoader )
    {
        if( !( classLoader instanceof URLClassLoader ) )
        {
            throw new IllegalArgumentException( "ClassLoader is not an instance of URLClassLoader" );
        }
        return new ArrayList<>( Arrays.asList( ( (URLClassLoader) classLoader ).getURLs() ) );
    }

    public static void printURLs( ClassLoader classLoader )
    {
        printURLs( classLoader, new PrintWriter( new OutputStreamWriter( System.out, UTF_8 ) ) );
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
        printLoadedClasses( classLoader, new PrintWriter( new OutputStreamWriter( System.out, UTF_8 ) ) );
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
