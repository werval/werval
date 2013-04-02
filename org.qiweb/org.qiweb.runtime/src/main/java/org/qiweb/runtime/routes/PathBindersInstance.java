package org.qiweb.runtime.routes;

import java.util.ArrayList;
import java.util.List;
import org.qiweb.api.routes.PathBinder;
import org.qiweb.api.routes.PathBinderException;
import org.qiweb.api.routes.PathBinders;

public class PathBindersInstance
    implements PathBinders
{

    private final List<PathBinder<?>> pathBinders = new ArrayList<>();

    public PathBindersInstance()
    {
    }

    public PathBindersInstance( List<PathBinder<?>> pathBinders )
    {
        this.pathBinders.addAll( pathBinders );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> T bind( Class<T> type, String pathParamName, String pathParamValue )
    {
        for( PathBinder<?> pathBinder : pathBinders )
        {
            if( pathBinder.accept( type ) )
            {
                return (T) pathBinder.bind( pathParamName, pathParamValue );
            }
        }
        throw new PathBinderException( "No PathBinder found for type: " + type );
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> String unbind( Class<T> type, String pathParamName, T pathParamValue )
    {
        for( PathBinder<?> pathBinder : pathBinders )
        {
            if( pathBinder.accept( type ) )
            {
                return ( (PathBinder<T>) pathBinder ).unbind( pathParamName, pathParamValue );
            }
        }
        throw new PathBinderException( "No PathBinder found for type: " + type );
    }
}
