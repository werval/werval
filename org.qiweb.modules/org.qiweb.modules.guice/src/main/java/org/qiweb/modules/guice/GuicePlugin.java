/*
 * Copyright (c) 2014 the original author or authors
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
package org.qiweb.modules.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.qiweb.api.Application;
import org.qiweb.api.Plugin;
import org.qiweb.api.exceptions.ActivationException;

/**
 * Guice Plugin.
 *
 * Expose {@link Injector} as API.
 */
public class GuicePlugin
    implements Plugin<Injector>
{
    private Injector injector;

    @Override
    public void onActivate( Application application )
        throws ActivationException
    {
        try
        {
            String moduleName = application.config().string( "guice.module" );
            Module module = (Module) Class.forName( moduleName, true, application.classLoader() ).newInstance();
            injector = Guice.createInjector( module );
        }
        catch( ClassNotFoundException | InstantiationException | IllegalAccessException ex )
        {
            throw new ActivationException( "GuicePlugin activation failed", ex );
        }
    }

    @Override
    public void onPassivate( Application application )
    {
        injector = null;
    }

    @Override
    public Class<Injector> apiType()
    {
        return Injector.class;
    }

    @Override
    public Injector api()
    {
        return injector;
    }
}
