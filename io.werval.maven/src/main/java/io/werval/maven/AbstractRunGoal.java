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
package io.werval.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Abstract Run Goal.
 */
public abstract class AbstractRunGoal
    extends AbstractMojo
{
    /**
     * Configuration resource name.
     * <p>
     * Loaded from the application classpath.
     */
    @Parameter( property = "werval.configResource" )
    protected String configResource;

    /**
     * Configuration file.
     */
    @Parameter( property = "werval.configFile" )
    protected File configFile;

    /**
     * Configuration URL.
     */
    @Parameter( property = "werval.configUrl" )
    protected URL configUrl;

    /**
     * Extra classpath files or directories paths, relative to the project base directory.
     */
    @Parameter( property = "werval.extraClassPath" )
    protected String[] extraClassPath;

    @Parameter( property = "project", required = true, readonly = true )
    protected MavenProject project;

    protected final URL[] runtimeClassPath()
        throws DependencyResolutionRequiredException, MalformedURLException
    {
        Set<URL> classPathSet = new LinkedHashSet<>();
        for( String runtimeClassPathElement : project.getRuntimeClasspathElements() )
        {
            classPathSet.add( new File( runtimeClassPathElement ).toURI().toURL() );
        }
        return classPathSet.toArray( new URL[ classPathSet.size() ] );
    }
}
