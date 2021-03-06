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
package io.werval.gradle;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Abstract Plugin Test.
 */
/* package */ abstract class AbstractPluginTest
    extends WervalPluginTest
{
    private static final Function<Dependency, String> TO_ARTIFACT_STRING = new Function<Dependency, String>()
    {
        @Override
        public String apply( Dependency dependency )
        {
            return dependency.getGroup() + ':' + dependency.getName() + ':' + dependency.getVersion();
        }
    };

    protected final List<String> artifactsOfConfiguration( String name )
    {
        Configuration config = project.getConfigurations().findByName( name );
        return config.getDependencies().stream().map( TO_ARTIFACT_STRING ).collect( Collectors.<String>toList() );
    }

    protected final Predicate<String> startsWith( final String start )
    {
        return new Predicate<String>()
        {
            @Override
            public boolean test( String string )
            {
                return string.startsWith( start );
            }
        };
    }

    @Test
    public void wervalDependencies()
    {
        assertTrue(
            artifactsOfConfiguration( "compile" ).stream().anyMatch(
                startsWith( "io.werval:io.werval.api" )
            )
        );
        assertTrue(
            artifactsOfConfiguration( "testCompile" ).stream().anyMatch(
                startsWith( "io.werval:io.werval.test" )
            )
        );
    }
}
