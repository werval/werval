/**
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
package org.qiweb.gradle

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * Dependencies extension to easily add QiWeb dependencies in Gradle builds.
 */
class QiWebDependencies
{
    public static final String GROUP = "org.qiweb"

    private final DependencyHandler dependencies

    QiWebDependencies( DependencyHandler dependencies )
    {
        this.dependencies = dependencies
    }

    Dependency getApi()
    {
        dependencies.create( "${GROUP}:io.werval.api:$BuildVersion.VERSION" )
    }

    Dependency getSpi()
    {
        dependencies.create( "${GROUP}:io.werval.spi:$BuildVersion.VERSION" )
    }

    Dependency module( String module )
    {
        dependency( "modules.${module}" )
    }

    Dependency getTest()
    {
        dependencies.create( "${GROUP}:io.werval.test:$BuildVersion.VERSION" )
    }

    Dependency getNettyServer()
    {
        dependencies.create( "${GROUP}:io.werval.server.bootstrap:$BuildVersion.VERSION" )
    }

    Dependency dependency( String name )
    {
        dependencies.create( "${GROUP}:${GROUP}.${name}:$BuildVersion.VERSION" )
    }
}
