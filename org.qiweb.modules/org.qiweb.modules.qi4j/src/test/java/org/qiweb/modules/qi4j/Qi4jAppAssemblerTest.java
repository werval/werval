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

import org.junit.ClassRule;
import org.junit.Test;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qiweb.test.QiWebRule;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class Qi4jAppAssemblerTest
{
    @ClassRule
    public static final QiWebRule QIWEB = new QiWebRule( "qi4j-assembler.conf" );

    @Test
    public void test()
    {
        Application qi4jApplication = QIWEB.application().plugin( Application.class );
        Module module = qi4jApplication.findModule( "Layer 1", "Module 1" );
        HelloWorld helloWorld = module.findService( HelloWorld.class ).get();
        assertThat( helloWorld.sayHello( "World" ), equalTo( "Hello World!" ) );
    }
}