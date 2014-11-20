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
import org.gradle.api.plugins.ApplicationPlugin;
import org.junit.Test;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * QiWebApplicationPlugin Test.
 */
public class QiWebApplicationPluginTest
    extends AbstractQiWebMorePluginTest
{
    @Override
    protected Plugin plugin()
    {
        return new QiWebApplicationPlugin();
    }

    @Test
    @Override
    public void qiwebDependencies()
    {
        super.qiwebDependencies();
        assertTrue(
            artifactsOfConfiguration( "runtime" ).stream().anyMatch(
                startsWith( "org.qiweb:org.qiweb.server.bootstrap" )
            )
        );
    }

    @Test
    public void applicationPlugin()
    {
        assertThat( project.getPlugins().findPlugin( ApplicationPlugin.class ), notNullValue() );
    }

    @Test
    public void stageTask()
    {
        assertThat( project.getTasks().getByName( "stage" ), notNullValue() );
    }
}
