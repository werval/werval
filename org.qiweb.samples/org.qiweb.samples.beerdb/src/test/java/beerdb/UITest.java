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
package beerdb;

import org.fluentlenium.adapter.FluentTest;
import org.junit.Rule;
import org.junit.Test;
import org.qiweb.test.QiWebRule;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class UITest
    extends FluentTest
{

    @Rule
    public QiWebRule qiwebRule = new QiWebRule();

    @Test
    public void testUI()
    {
        // First load to breweries
        goTo( qiwebRule.baseHttpUrl() + "/" );
        assertThat( title(), equalTo( "Beer Database - QiWeb Sample App" ) );
        assertThat( findFirst( "ul.navbar-nav li" ).getAttribute( "class" ), equalTo( "active" ) );
        assertThat( findFirst( "#breweries" ), notNullValue() );
    }
}
