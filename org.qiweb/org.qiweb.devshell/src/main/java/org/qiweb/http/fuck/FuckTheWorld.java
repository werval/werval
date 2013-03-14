package org.qiweb.http.fuck;

import java.net.URLClassLoader;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.classworlds.DuplicateRealmException;

public class FuckTheWorld
{

    public static void main( String[] args )
        throws DuplicateRealmException
    {
        ClassWorld world = new ClassWorld();
        ( (URLClassLoader) world.getClass().getClassLoader() ).getURLs();
        ClassRealm qiWebRealm = world.newRealm( "qiweb" );
        qiWebRealm.addConstituent( null );
    }
}
