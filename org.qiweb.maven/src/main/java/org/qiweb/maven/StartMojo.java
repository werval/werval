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
package org.qiweb.maven;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.qiweb.commands.StartCommand;

import static org.apache.maven.plugins.annotations.LifecyclePhase.COMPILE;
import static org.apache.maven.plugins.annotations.ResolutionScope.RUNTIME;

/**
 * Run the application in production mode.
 */
@Mojo( name = "start", requiresDependencyResolution = RUNTIME, threadSafe = true )
@Execute( phase = COMPILE )
public class StartMojo
    extends AbstractQiWebMojo
{
    /**
     * Main class.
     */
    @Parameter( property = "qiwebstart.mainClass", defaultValue = "org.qiweb.server.bootstrap.Main" )
    private String mainClass;

    /**
     * Main class arguments.
     */
    @Parameter( property = "qiwebstart.arguments" )
    private String[] arguments;

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        getLog().info( ">> QiWeb Production Mode for " + project.getArtifactId() + " starting..." );

        if( null == arguments )
        {
            arguments = new String[ 0 ];
        }
        if( null == extraClassPath )
        {
            extraClassPath = new String[ 0 ];
        }

        try
        {
            Set<URL> runtimeCP = new LinkedHashSet<>();
            runtimeCP.addAll( Arrays.asList( runtimeClassPath() ) );
            for( String extraCP : extraClassPath )
            {
                runtimeCP.add( new File( project.getBasedir(), extraCP ).toURI().toURL() );
            }
            URL[] runtimeClassPath = runtimeCP.toArray( new URL[ runtimeCP.size() ] );

            if( getLog().isDebugEnabled() )
            {
                StringBuffer msg = new StringBuffer( "Invoking : " );
                msg.append( mainClass );
                msg.append( ".main(" );
                for( int idx = 0; idx < arguments.length; idx++ )
                {
                    if( idx > 0 )
                    {
                        msg.append( ", " );
                    }
                    msg.append( arguments[idx] );
                }
                msg.append( ")" );
                getLog().debug( msg );
            }

            new StartCommand(
                StartCommand.ExecutionModel.ISOLATED_THREADS,
                mainClass,
                arguments,
                runtimeClassPath,
                configResource,
                configFile,
                configUrl
            ).run();
        }
        catch( Exception ex )
        {
            throw new MojoExecutionException( ex.getMessage(), ex );
        }
    }
}
