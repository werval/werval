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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.qiweb.runtime.util.ClassLoaders;

/**
 * AbstractQiWebMojo.
 */
public abstract class AbstractQiWebMojo
    extends AbstractMojo
{
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
        File qiwebDoc = ClassLoaders.classpathForResource(
            getClass().getClassLoader(),
            "org/qiweb/doc/html/index.html"
        );
        if( qiwebDoc != null )
        {
            classPathSet.add( qiwebDoc.toURI().toURL() );
        }
        else
        {
            getLog().warn(
                "QiWeb Documentation not in the Maven Plugin Classpath, please report the issue: "
                + "https://scm.codeartisans.org/qiweb/qiweb/issues/new"
            );
        }
        return classPathSet.toArray( new URL[ classPathSet.size() ] );
    }
}