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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.api.routes.ReverseRoute;
import org.qiweb.test.AbstractQiWebTest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.qiweb.api.controllers.Controller.*;
import static org.qiweb.api.routes.ReverseRoutes.*;

public class ReverseRoutesTest
    extends AbstractQiWebTest
{

    public static class Controller
    {

        public Outcome simpleMethod()
        {
            ReverseRoute reverseRoute = reverseRoutes().of( GET( Controller.class ).simpleMethod() );
            return outcomes().ok( reverseRoute.httpUrl() ).build();
        }

        public Outcome simpleMethod( String param )
        {
            ReverseRoute reverseRoute = reverseRoutes().of( GET( Controller.class ).simpleMethod( param ) );
            return outcomes().ok( reverseRoute.httpUrl() ).build();
        }

        public Outcome wild( String card )
        {
            ReverseRoute reverseRoute = reverseRoutes().of( GET( Controller.class ).wild( card ) );
            return outcomes().ok( reverseRoute.httpUrl() ).build();
        }

        public Outcome qstring( String path, String qsOne, String qsTwo )
        {
            ReverseRoute reverseRoute = reverseRoutes().of( GET( Controller.class ).qstring( path, qsOne, qsTwo ) );
            return outcomes().ok( reverseRoute.httpUrl() ).build();
        }

        public Outcome appendedQueryString()
        {
            ReverseRoute reverseRoute = reverseRoutes().of( GET( Controller.class ).appendedQueryString() ).appendQueryString( request().queryString().asMapAll() );
            return outcomes().ok( reverseRoute.httpUrl() ).build();
        }

        public Outcome fragmentIdentifier()
        {
            ReverseRoute reverseRoute = reverseRoutes().of( GET( Controller.class ).fragmentIdentifier() ).withFragmentIdentifier( "bazar" );
            return outcomes().ok( reverseRoute.httpUrl() ).build();
        }
    }

    @Override
    protected String routesString()
    {
        return "GET /simpleMethod org.qiweb.runtime.routes.ReverseRoutesTest$Controller.simpleMethod\n"
               + "GET /simpleMethod/:param/foo org.qiweb.runtime.routes.ReverseRoutesTest$Controller.simpleMethod( String param )\n"
               + "GET /wild/*card org.qiweb.runtime.routes.ReverseRoutesTest$Controller.wild( String card )\n"
               + "GET /query/:path/string org.qiweb.runtime.routes.ReverseRoutesTest$Controller.qstring( String path, String qsOne, String qsTwo )\n"
               + "GET /appended/qs org.qiweb.runtime.routes.ReverseRoutesTest$Controller.appendedQueryString\n"
               + "GET /fragment/identifier org.qiweb.runtime.routes.ReverseRoutesTest$Controller.fragmentIdentifier";
    }

    @Test
    public void testSimpleMethod()
        throws Exception
    {
        HttpClient client = newHttpClientInstance();
        String httpUrl = BASE_URL + "simpleMethod";
        HttpResponse response = client.execute( new HttpGet( httpUrl ) );
        assertThat( response.getStatusLine().getStatusCode(), is( 200 ) );
        assertThat( responseBodyAsString( response ), equalTo( httpUrl ) );
    }

    @Test
    public void testSimpleMethodWithParam()
        throws Exception
    {
        HttpClient client = newHttpClientInstance();
        String httpUrl = BASE_URL + "simpleMethod/test/foo";
        HttpResponse response = client.execute( new HttpGet( httpUrl ) );
        assertThat( response.getStatusLine().getStatusCode(), is( 200 ) );
        assertThat( responseBodyAsString( response ), equalTo( httpUrl ) );
    }

    @Test
    public void testWildcard()
        throws Exception
    {
        HttpClient client = newHttpClientInstance();
        String httpUrl = BASE_URL + "wild/wild/wild/card";
        HttpResponse response = client.execute( new HttpGet( httpUrl ) );
        assertThat( response.getStatusLine().getStatusCode(), is( 200 ) );
        assertThat( responseBodyAsString( response ), equalTo( httpUrl ) );
    }

    @Test
    public void testQueryString()
        throws Exception
    {
        HttpClient client = newHttpClientInstance();
        String httpUrl = BASE_URL + "query/foo/string?qsOne=bar&qsTwo=bazar";
        HttpResponse response = client.execute( new HttpGet( httpUrl ) );
        assertThat( response.getStatusLine().getStatusCode(), is( 200 ) );
        assertThat( responseBodyAsString( response ), equalTo( httpUrl ) );
    }

    @Test
    public void testAppendedQueryString()
        throws Exception
    {
        HttpClient client = newHttpClientInstance();
        String httpUrl = BASE_URL + "appended/qs?bar=bazar&foo=bar&foo=baz";
        HttpResponse response = client.execute( new HttpGet( httpUrl ) );
        assertThat( response.getStatusLine().getStatusCode(), is( 200 ) );
        assertThat( responseBodyAsString( response ), equalTo( httpUrl ) );
    }

    @Test
    public void testFragmentIdentifier()
        throws Exception
    {
        HttpClient client = newHttpClientInstance();
        String httpUrl = BASE_URL + "fragment/identifier";
        HttpResponse response = client.execute( new HttpGet( httpUrl ) );
        assertThat( response.getStatusLine().getStatusCode(), is( 200 ) );
        assertThat( responseBodyAsString( response ), equalTo( httpUrl + "#bazar" ) );
    }
}
