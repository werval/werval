/*
 * Copyright (c) 2014-2015 the original author or authors
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
package io.werval.modules.zest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.json.JSONException;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.builder.ApplicationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin for Zest Application.
 * <p>
 * See the Zest documentation at <a href="https://zest.apache.org/">zest.apache.org</a>.
 */
public class ZestPlugin
    implements io.werval.api.Plugin<org.qi4j.api.structure.Application>
{
    private static final Logger LOG = LoggerFactory.getLogger( ZestPlugin.class );
    private org.qi4j.api.structure.Application zestApplication;

    @Override
    public Class<org.qi4j.api.structure.Application> apiType()
    {
        return org.qi4j.api.structure.Application.class;
    }

    @Override
    public org.qi4j.api.structure.Application api()
    {
        return zestApplication;
    }

    @Override
    public void onActivate( io.werval.api.Application application )
        throws io.werval.api.exceptions.ActivationException
    {
        boolean hasAssembler = application.config().has( "zest.assembler" );
        boolean hasAssembly = application.config().has( "zest.assembly" );
        if( hasAssembler && hasAssembly )
        {
            throw new io.werval.api.exceptions.ActivationException(
                "Zest Plugin cannot have both 'zest.assembler' and 'zest.assembly' configuration properties set, "
                + "remove one and retry."
            );
        }
        if( !hasAssembler && !hasAssembly )
        {
            LOG.warn(
                "Zest Plugin has no 'zest.assembler' nor 'zest.assembly' configuration property set. "
                + "Zest Application will not be created and activated."
            );
            return;
        }
        try
        {
            if( hasAssembler )
            {
                String assembler = application.config().string( "zest.assembler" );
                ApplicationAssembler appAssembler = createApplicationAssembler( application, assembler );
                Energy4Java zest = new Energy4Java();
                ApplicationDescriptor model = zest.newApplicationModel( appAssembler );
                zestApplication = model.newInstance( zest.api() );
                zestApplication.activate();
                LOG.debug( "Zest Application assembled from '{}' successfuly activated", assembler );
            }
            else
            {
                // TODO Support application modes!
                String assembly = application.config().string( "zest.assembly" );
                try( InputStream json = application.classLoader().getResourceAsStream( assembly ) )
                {
                    zestApplication = ApplicationBuilder.fromJson( json ).newApplication();
                }
                LOG.debug( "Zest Application assembled from '{}' successfuly activated", assembly );
            }
        }
        catch( JSONException | IOException |
               org.qi4j.bootstrap.AssemblyException | org.qi4j.api.activation.ActivationException ex )
        {
            throw new io.werval.api.exceptions.ActivationException( ex.getMessage(), ex );
        }
    }

    private ApplicationAssembler createApplicationAssembler( io.werval.api.Application application, String assembler )
    {
        try
        {
            Class<?> assemblerClass = application.classLoader().loadClass( assembler );
            if( !ApplicationAssembler.class.isAssignableFrom( assemblerClass ) )
            {
                throw new IllegalArgumentException( assembler + " is not an ApplicationAssembler." );
            }
            try
            {
                org.qi4j.api.structure.Application.Mode mode;
                switch( application.mode() )
                {
                    case DEV:
                        mode = org.qi4j.api.structure.Application.Mode.development;
                        break;
                    case TEST:
                        mode = org.qi4j.api.structure.Application.Mode.test;
                        break;
                    case PROD:
                    default:
                        mode = org.qi4j.api.structure.Application.Mode.production;
                        break;
                }
                return (ApplicationAssembler) assemblerClass
                    .getConstructor( org.qi4j.api.structure.Application.Mode.class )
                    .newInstance( mode );
            }
            catch( NoSuchMethodException ex )
            {
                return (ApplicationAssembler) assemblerClass.newInstance();
            }
        }
        catch( ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException ex )
        {
            throw new io.werval.api.exceptions.ActivationException( ex.getMessage(), ex );
        }
    }

    @Override
    public void onPassivate( io.werval.api.Application application )
    {
        if( zestApplication != null )
        {
            try
            {
                zestApplication.passivate();
            }
            catch( org.qi4j.api.activation.PassivationException ex )
            {
                throw new io.werval.api.exceptions.PassivationException( ex.getMessage(), ex );
            }
            finally
            {
                zestApplication = null;
            }
        }
    }
}
