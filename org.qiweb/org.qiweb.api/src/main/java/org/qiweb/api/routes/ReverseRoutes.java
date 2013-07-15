package org.qiweb.api.routes;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.exceptions.QiWebException;
import org.qiweb.api.http.Headers;

/**
 * Reverse Routes.
 */
// TODO Remove Javassist dependency in API!
public abstract class ReverseRoutes
{

    /**
     * Get the ReverseRoute of a Controller call.
     * <p>See {@link Plop}.</p>
     * @param reverseOutcome Reverse Outcome
     * @return a ReverseRoute
     * @throws IllegalArgumentException when the given Outcome is not appropriate
     */
    public abstract ReverseRoute of( Outcome reverseOutcome );

    public static <T> T GET( Class<T> controllerType )
    {
        return HTTP( "GET", controllerType );
    }

    public static <T> T POST( Class<T> controllerType )
    {
        return HTTP( "GET", controllerType );
    }

    @SuppressWarnings( "unchecked" )
    public static <T> T HTTP( final String httpMethod, final Class<T> controllerType )
    {
        T controllerProxy;
        if( controllerType.isInterface() )
        {
            controllerProxy = (T) java.lang.reflect.Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[]
            {
                controllerType
            },
                new InvocationHandler()
            {
                @Override
                public Object invoke( Object proxy, Method controllerMethod, Object[] args )
                {
                    return new ReverseOutcome( httpMethod, controllerType, controllerMethod, Arrays.asList( args ) );
                }
            } );
        }
        else
        {
            try
            {
                ProxyFactory proxyFactory = new ProxyFactory();
                proxyFactory.setSuperclass( controllerType );
                controllerProxy = (T) proxyFactory.createClass().newInstance();
                ( (Proxy) controllerProxy ).setHandler( new MethodHandler()
                {
                    @Override
                    public Object invoke( Object self, Method controllerMethod, Method proceed, Object[] args )
                        throws Throwable
                    {
                        return new ReverseOutcome( httpMethod, controllerType, controllerMethod, Arrays.asList( args ) );
                    }
                } );
            }
            catch( InstantiationException | IllegalAccessException ex )
            {
                throw new QiWebException( "Unable to reverse route", ex );
            }
        }
        return controllerProxy;
    }

    public static final class ReverseOutcome
        implements Outcome
    {

        private final String httpMethod;
        private final Class<?> controllerType;
        private final Method controllerMethod;
        private final List<Object> parameters;

        private ReverseOutcome( String method, Class<?> controllerType, Method controllerMethod, List<Object> parameters )
        {
            this.httpMethod = method;
            this.controllerType = controllerType;
            this.controllerMethod = controllerMethod;
            this.parameters = parameters;
        }

        public String httpMethod()
        {
            return httpMethod;
        }

        public Class<?> controllerType()
        {
            return controllerType;
        }

        public Method controllerMethod()
        {
            return controllerMethod;
        }

        public List<Object> parameters()
        {
            return Collections.unmodifiableList( parameters );
        }

        @Override
        public int status()
        {
            throw new UnsupportedOperationException( "ReverseOutcome has no status." );
        }

        @Override
        public StatusClass statusClass()
        {
            throw new UnsupportedOperationException( "ReverseOutcome has no status." );
        }

        @Override
        public Headers headers()
        {
            throw new UnsupportedOperationException( "ReverseOutcome has no headers." );
        }
    }
}