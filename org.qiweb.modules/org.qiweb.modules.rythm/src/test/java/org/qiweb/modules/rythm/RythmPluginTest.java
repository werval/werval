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
package org.qiweb.modules.rythm;

import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.test.QiWebHttpRule;
import org.rythmengine.RythmEngine;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

public class RythmPluginTest
{
    @ClassRule
    public static final QiWebHttpRule QIWEB = new QiWebHttpRule();

    @Test
    public void testPlugin()
    {
        RythmEngine rythm = QIWEB.application().plugin( RythmEngine.class );
        String output = rythm.render( "hello.html", "World" );
        System.out.println( output );
        assertThat( output, containsString( "<title>Hello World</title>" ) );
        assertThat( output, containsString( "<h1>Hello World</h1>" ) );
    }
}
