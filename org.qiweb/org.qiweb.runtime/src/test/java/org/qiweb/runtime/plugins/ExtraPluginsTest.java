/**
 * Copyright (c) 2013 the original author or authors
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
package org.qiweb.runtime.plugins;

import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.test.QiWebRule;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Assert Extra-Plugins defined in a Global Object are properly used.
 */
public class ExtraPluginsTest
{

    @ClassRule
    public static final QiWebRule QIWEB = new QiWebRule( "extra-plugin-test.conf" );

    @Test
    public void givenRegisteredPluginWhenUsePluginExpectCorrectResult()
    {
        String hello = QIWEB.application().plugin( HelloWorld.class ).sayHello( "John" );
        assertThat( hello, equalTo( "Hello John!" ) );
    }

}
