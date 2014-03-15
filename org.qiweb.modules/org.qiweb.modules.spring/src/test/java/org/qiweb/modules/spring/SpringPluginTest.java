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
package org.qiweb.modules.spring;

import app.TestFilter;
import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.test.QiWebRule;

import static com.jayway.restassured.RestAssured.expect;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * SpringPluginTest.
 */
public class SpringPluginTest
{
    @ClassRule
    public static final QiWebRule QIWEB = new QiWebRule( new RoutesParserProvider( "GET / app.TestController.index" ) );

    @Test
    public void spring()
    {
        expect()
            .statusCode( 200 )
            .body( equalTo( "Hello World!" ) )
            .when()
            .get( "/" );
        assertThat( TestFilter.invocations, is( 1 ) );
    }
}
