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
package org.qiweb.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * QiWebPlugin Test.
 */
public class QiWebPluginTest
{
    protected Project project;

    @Before
    public void before()
    {
        project = ProjectBuilder.builder().build();
        plugin().apply( project );
    }

    protected Plugin plugin()
    {
        return new QiWebPlugin();
    }

    @Test
    public void javaPlugin()
    {
        assertThat(
            project.getPlugins().findPlugin( JavaPlugin.class ),
            notNullValue()
        );
    }

    @Test
    public void dependenciesExtension()
    {
        assertThat( project.getExtensions().getByName( "qiweb" ), notNullValue() );
        assertThat( project.getExtensions().getByType( QiWebDependencies.class ), notNullValue() );
    }

    @Test
    public void secretStartAndDevShellTasks()
    {
        assertThat( project.getTasks().getByName( "secret" ), instanceOf( QiWebSecretTask.class ) );
        assertThat( project.getTasks().getByName( "start" ), instanceOf( QiWebStartTask.class ) );
        assertThat( project.getTasks().getByName( "devshell" ), instanceOf( QiWebDevShellTask.class ) );
        assertThat( project.getTasks().getByName( "devshell_rebuild" ), instanceOf( QiWebRebuildTask.class ) );
    }
}
