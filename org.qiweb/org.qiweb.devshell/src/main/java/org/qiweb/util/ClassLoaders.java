package org.qiweb.util;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.codeartisans.java.toolbox.Strings;

public class ClassLoaders
{

    public static void printClassLoaders( ClassLoader classLoader )
    {
        printClassLoaders( classLoader, new PrintWriter( System.out ) );
    }

    @SuppressWarnings(
        {
        "unchecked",
        "UseOfObsoleteCollectionType"
    } )
    public static void printClassLoaders( ClassLoader classLoader, PrintWriter output )
    {
        try
        {
            Field clClassesField = ClassLoader.class.getDeclaredField( "classes" );
            clClassesField.setAccessible( true );
            ClassLoader cl = classLoader;
            int idx = 0;
            while( cl != null )
            {
                String tab = "    ";
                String[] indents = new String[ idx ];
                Arrays.fill( indents, tab );
                String indent = Strings.join( indents ) + "+ ";
                output.println( indent + " " + cl );
                java.util.Vector<Class<?>> classes = (java.util.Vector<Class<?>>) clClassesField.get( cl );
                List<String> classNames = new ArrayList<String>();
                for( Class<?> clazz : classes )
                {
                    classNames.add( clazz.getName() );
                }
                Collections.sort( classNames );
                for( String className : classNames )
                {
                    output.println( indent + tab + className );
                }
                cl = cl.getParent();
                idx++;
            }
            clClassesField.setAccessible( false );
            output.flush();
        }
        catch( Exception ex )
        {
            throw new RuntimeException( "Unable to print ClassLoaders hierarchy: " + ex.getMessage(), ex );
        }
    }

    private ClassLoaders()
    {
    }
}
