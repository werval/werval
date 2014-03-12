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
package org.qiweb.modules.qi4j;

import java.io.IOException;
import java.io.InputStream;
import org.json.JSONException;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.builder.ApplicationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * QiWeb Plugin for Qi4j Application.
 *
 * See the Qi4j documentation at <a href="http://qi4j.org/">qi4j.org</a>.
 */
public class Qi4jPlugin
    extends org.qiweb.api.PluginAdapter<org.qi4j.api.structure.Application>
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
    public void onActivate( org.qiweb.api.Application application )
        throws org.qiweb.api.exceptions.ActivationException
    {
        boolean hasAssembler = application.config().has( "qi4j.assembler" );
        boolean hasAssembly = application.config().has( "qi4j.assembly" );
        if( hasAssembler && hasAssembly )
        {
            throw new org.qiweb.api.exceptions.ActivationException(
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
                Object instance = application.classLoader().loadClass( assembler ).newInstance();
                if( !ApplicationAssembler.class.isAssignableFrom( instance.getClass() ) )
                {
                    throw new IllegalArgumentException( assembler + " is not an ApplicationAssembler." );
                }
                ApplicationAssembler appAssembler = (ApplicationAssembler) instance;
                Energy4Java qi4j = new Energy4Java();
                ApplicationDescriptor model = qi4j.newApplicationModel( appAssembler );
                qi4jApplication = model.newInstance( qi4j.api() );
                qi4jApplication.activate();
                LOG.debug( "Qi4j Application assembled from '{}' successfuly activated", assembler );
            }
            else
            {
                String assembly = application.config().string( "qi4j.assembly" );
                try( InputStream json = application.classLoader().getResourceAsStream( assembly ) )
                {
                    qi4jApplication = ApplicationBuilder.fromJson( json ).newApplication();
                }
                LOG.debug( "Qi4j Application assembled from '{}' successfuly activated", assembly );
            }
        }
        catch( ClassNotFoundException | InstantiationException | IllegalAccessException |
               JSONException | IOException |
               org.qi4j.bootstrap.AssemblyException | org.qi4j.api.activation.ActivationException ex )
        {
            throw new org.qiweb.api.exceptions.ActivationException( ex.getMessage(), ex );
        }
    }

    @Override
    public void onPassivate( org.qiweb.api.Application application )
    {
        if( qi4jApplication != null )
        {
            try
            {
                qi4jApplication.passivate();
            }
            catch( org.qi4j.api.activation.PassivationException ex )
            {
                throw new org.qiweb.api.exceptions.PassivationException( ex.getMessage(), ex );
            }
            finally
            {
                qi4jApplication = null;
            }
        }
    }
}
