/*
 * Copyright (c) 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qiweb.runtime.routes;

import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.api.http.Status;
import org.qiweb.spi.http.HttpBuilders.RequestBuilder;
import org.qiweb.test.QiWebRule;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class RouterTest
{
    @ClassRule
    public static final QiWebRule QIWEB = new QiWebRule( new RoutesParserProvider(
        "GET / com.acme.app.FakeControllerInstance.index()\n"
        + "GET /foo com.acme.app.FakeControllerInstance.foo()\n"
        + "GET /:id/:slug com.acme.app.FakeControllerInstance.another( String id, Integer slug )"
    ) );

    @Test
    public void testRoutes()
        throws Exception
    {
        RequestBuilder builder = QIWEB.newRequestBuilder();
        assertThat(
            QIWEB.application().handleRequest( builder.get( "/" ).build() ).responseHeader().status(),
            equalTo( Status.OK )
        );
        assertThat(
            QIWEB.application().handleRequest( builder.post( "/" ).build() ).responseHeader().status(),
            equalTo( Status.NOT_FOUND )
        );
        assertThat(
            QIWEB.application().handleRequest( builder.get( "/foo" ).build() ).responseHeader().status(),
            equalTo( Status.OK )
        );
        assertThat(
            QIWEB.application().handleRequest( builder.get( "/bazar" ).build() ).responseHeader().status(),
            equalTo( Status.NOT_FOUND )
        );
        assertThat(
            QIWEB.application().handleRequest( builder.get( "/azertyuiop/1234" ).build() ).responseHeader().status(),
            equalTo( Status.OK )
        );
    }
}
