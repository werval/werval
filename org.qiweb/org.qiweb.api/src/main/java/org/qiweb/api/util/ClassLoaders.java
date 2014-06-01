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
package org.qiweb.api.util;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.qiweb.api.exceptions.QiWebException;

import static org.qiweb.api.util.Charsets.UTF_8;

/**
 * ClassLoader utilities.
 */
public final class ClassLoaders
{
    /**
     * Check if a resource exists.
     *
     * Reliabily report directories as non existing resources whether the classpath is a directory or a jar file.
     *
     * @param loader ClassLoader
     * @param path   Resource path
     *
     * @return {@literal true} if the resource exists, {@literal false} otherwise
     */
    public static boolean resourceExists( ClassLoader loader, String path )
    {
        URL resource = loader.getResource( path );
        if( resource == null )
        {
            return false;
        }
        try
        {
            if( new File( resource.toURI() ).isDirectory() )
            {
                return false;
            }
        }
        catch( URISyntaxException ex )
        {
            try
            {
                if( new File( resource.getPath() ).isDirectory() )
                {
                    return false;
                }
            }
            catch( IllegalArgumentException skip )
            {
                return false;
            }
        }
        return true;
    }

    public static List<URL> urlsOf( ClassLoader classLoader )
    {
        if( !( classLoader instanceof URLClassLoader ) )
        {
            throw new IllegalArgumentException( "ClassLoader is not an instance of URLClassLoader" );
        }
        return new ArrayList<>( Arrays.asList( ( (URLClassLoader) classLoader ).getURLs() ) );
    }

    public static File classpathForClass( Class<?> targetClass )
    {
        URI location;
        try
        {
            location = targetClass.getProtectionDomain().getCodeSource().getLocation().toURI();
        }
        catch( URISyntaxException e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
        if( !location.getScheme().equals( "file" ) )
        {
            throw new QiWebException(
                String.format(
                    "Cannot determine classpath for %s from codebase '%s'.",
                    targetClass.getName(),
                    location
                )
            );
        }
        return new File( location.getPath() );
    }

    public static File classpathForResource( String name )
    {
        return classpathForResource( (ClassLoader) null, name );
    }

    public static File classpathForResource( ClassLoader classLoader, String name )
    {
        if( classLoader == null )
        {
            return classpathForResource( ClassLoader.getSystemResource( name ), name );
        }
        else
        {
            return classpathForResource( classLoader.getResource( name ), name );
        }
    }

    public static File classpathForResource( URL resource, String name )
    {
        URI location;
        try
        {
            location = resource.toURI();
            switch( location.getScheme() )
            {
                case "file":
                    String path = location.getPath();
                    assert path.endsWith( "/" + name );
                    return new File( path.substring( 0, path.length() - ( name.length() + 1 ) ) );
                case "jar":
                    String schemeSpecificPart = location.getRawSchemeSpecificPart();
                    int pos = schemeSpecificPart.indexOf( "!" );
                    if( pos > 0 )
                    {
                        assert schemeSpecificPart.substring( pos + 1 ).equals( "/" + name );
                        URI jarFile = new URI( schemeSpecificPart.substring( 0, pos ) );
                        if( jarFile.getScheme().equals( "file" ) )
                        {
                            return new File( jarFile.getPath() );
                        }
                    }
                    break;
                default:
            }
        }
        catch( URISyntaxException e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }
        throw new QiWebException(
            String.format( "Cannot determine classpath for resource '%s' from location '%s'.", name, location )
        );
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
                String indent = Strings.indentTab( "+ ", idx );
                output.println( indent + urlLoader );
                for( URL url : urlLoader.getURLs() )
                {
                    output.println( indent + Strings.TAB + url );
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
                    output.println( indent + Strings.TAB + "Not URLClassLoader parent: " + parent );
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
                String indent = Strings.indentTab( "+ ", idx );
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
                    output.println( indent + Strings.TAB + className );
                }
                cl = cl.getParent();
                idx++;
            }
            clClassesField.setAccessible( false );
            output.flush();
        }
        catch( NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex )
        {
            throw new QiWebException(
                "Unable to print loaded classes from ClassLoaders hierarchy: " + ex.getMessage(),
                ex
            );
        }
    }

    private ClassLoaders()
    {
    }
}
