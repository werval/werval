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
package io.werval.modules.zest;

import org.qi4j.api.structure.Application;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

public class HelloWorldAppAssembler
    implements ApplicationAssembler
{
    private final Application.Mode mode;

    public HelloWorldAppAssembler()
    {
        this.mode = Application.Mode.production;
    }

    public HelloWorldAppAssembler( Application.Mode mode )
    {
        this.mode = mode;
    }

    @Override
    public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
        throws AssemblyException
    {
        ApplicationAssembly assembly = applicationFactory.newApplicationAssembly();
        assembly.setName( "Zest Application Under Test" );
        assembly.setMode( mode );
        ModuleAssembly module = assembly.layer( "Layer 1" ).module( "Module 1" );
        new HelloWorldAssembler().assemble( module );
        return assembly;
    }
}
