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
package io.werval.gradle

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * Dependencies extension to easily add Werval dependencies in Gradle builds.
 */
class WervalDependencies
{
    public static final String GROUP = "io.werval"

    private final DependencyHandler dependencies

    WervalDependencies( DependencyHandler dependencies )
    {
        this.dependencies = dependencies
    }

    Dependency getApi()
    {
        dependency( "api" )
    }

    Dependency getSpi()
    {
        dependency( "spi" )
    }

    Dependency module( String module )
    {
        dependency( "modules.${module}" )
    }

    Dependency getTest()
    {
        dependency( "test" )
    }

    Dependency getNettyServer()
    {
        dependency( "server.bootstrap" )
    }

    Dependency dependency( String name )
    {
        dependencies.create( "${GROUP}:${GROUP}.${name}:$BuildVersion.VERSION" )
    }
}
