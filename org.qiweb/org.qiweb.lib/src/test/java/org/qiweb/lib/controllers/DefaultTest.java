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
package org.qiweb.lib.controllers;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import org.qiweb.test.AbstractQiWebTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class DefaultTest
    extends AbstractQiWebTest
{

    @Override
    protected String routesString()
    {
        return "GET /notFound org.qiweb.lib.controllers.Default.notFound\n"
               + "GET /internalServerError org.qiweb.lib.controllers.Default.internalServerError\n"
               + "GET /notImplemented org.qiweb.lib.controllers.Default.notImplemented";
    }

    @Test
    public void givenNotFoundRouteWhenRequestingExpectNotFound()
        throws Exception
    {
        HttpResponse response = newHttpClientInstance().execute( new HttpGet( BASE_URL + "notFound" ) );
        assertThat( response.getStatusLine().getStatusCode(), equalTo( 404 ) );
    }

    @Test
    public void givenInternalServerErrorRouteWhenRequestingExpectInternalServerError()
        throws Exception
    {
        HttpResponse response = newHttpClientInstance().execute( new HttpGet( BASE_URL + "internalServerError" ) );
        assertThat( response.getStatusLine().getStatusCode(), equalTo( 500 ) );
    }

    @Test
    public void givenNotImplementedRouteWhenRequestingExpectNotImplemented()
        throws Exception
    {
        HttpResponse response = newHttpClientInstance().execute( new HttpGet( BASE_URL + "notImplemented" ) );
        assertThat( response.getStatusLine().getStatusCode(), equalTo( 501 ) );
    }
}
