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
package io.werval.modules.qi4j;

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
 * Plugin for Qi4j Application.
 * <p>
 * See the Qi4j documentation at <a href="http://qi4j.org/">qi4j.org</a>.
 */
public class Qi4jPlugin
    implements io.werval.api.Plugin<org.qi4j.api.structure.Application>
{
    private static final Logger LOG = LoggerFactory.getLogger( Qi4jPlugin.class );
    private org.qi4j.api.structure.Application qi4jApplication;

    @Override
    public Class<org.qi4j.api.structure.Application> apiType()
    {
        return org.qi4j.api.structure.Application.class;
    }

    @Override
    public org.qi4j.api.structure.Application api()
    {
        return qi4jApplication;
    }

    @Override
    public void onActivate( io.werval.api.Application application )
        throws io.werval.api.exceptions.ActivationException
    {
        boolean hasAssembler = application.config().has( "qi4j.assembler" );
        boolean hasAssembly = application.config().has( "qi4j.assembly" );
        if( hasAssembler && hasAssembly )
        {
            throw new io.werval.api.exceptions.ActivationException(
                "Qi4j Plugin cannot have both 'qi4j.assembler' and 'qi4j.assembly' configuration properties set, "
                + "remove one and retry."
            );
        }
        if( !hasAssembler && !hasAssembly )
        {
            LOG.warn(
                "Qi4j Plugin has no 'qi4j.assembler' nor 'qi4j.assembly' configuration property set. "
                + "Qi4j Application will not be created and activated."
            );
            return;
        }
        try
        {
            if( hasAssembler )
            {
                String assembler = application.config().string( "qi4j.assembler" );
                ApplicationAssembler appAssembler = createApplicationAssembler( application, assembler );
                Energy4Java qi4j = new Energy4Java();
                ApplicationDescriptor model = qi4j.newApplicationModel( appAssembler );
                qi4jApplication = model.newInstance( qi4j.api() );
                qi4jApplication.activate();
                LOG.debug( "Qi4j Application assembled from '{}' successfuly activated", assembler );
            }
            else
            {
                // TODO Support application modes!
                String assembly = application.config().string( "qi4j.assembly" );
                try( InputStream json = application.classLoader().getResourceAsStream( assembly ) )
                {
                    qi4jApplication = ApplicationBuilder.fromJson( json ).newApplication();
                }
                LOG.debug( "Qi4j Application assembled from '{}' successfuly activated", assembly );
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
        if( qi4jApplication != null )
        {
            try
            {
                qi4jApplication.passivate();
            }
            catch( org.qi4j.api.activation.PassivationException ex )
            {
                throw new io.werval.api.exceptions.PassivationException( ex.getMessage(), ex );
            }
            finally
            {
                qi4jApplication = null;
            }
        }
    }
}
