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
package io.werval.doc;

import io.werval.test.WervalHttpRule;
import org.junit.ClassRule;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.expect;

/**
 * Documentation Plugin Test.
 */
public class DocumentationPluginTest
{
    @ClassRule
    public static final WervalHttpRule WERVAL = new WervalHttpRule();

    @Test
    public void coreDocumentations()
    {
        expect()
            .statusCode( 200 )
            .when()
            .get( "/@doc" );

        expect()
            .statusCode( 200 )
            .when()
            .get( "/@doc/index.html" );
    }

    @Test
    public void dyamicDocumentations()
    {
        expect()
            .statusCode( 200 )
            .when()
            .get( "/@doc/modules/index.html" );
    }
}
