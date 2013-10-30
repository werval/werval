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
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.api.routes.ReverseRoute;
import org.qiweb.test.QiWebRule;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.qiweb.api.context.CurrentContext.outcomes;
import static org.qiweb.api.context.CurrentContext.request;
import static org.qiweb.api.context.CurrentContext.reverseRoutes;
import static org.qiweb.api.routes.ReverseRoutes.GET;

public class ReverseRoutesTest
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
            ReverseRoute reverseRoute = reverseRoutes().of( GET( Controller.class ).appendedQueryString() ).appendQueryString( request().queryString().allValues() );
            return outcomes().ok( reverseRoute.httpUrl() ).build();
        }

        public Outcome fragmentIdentifier()
        {
            ReverseRoute reverseRoute = reverseRoutes().of( GET( Controller.class ).fragmentIdentifier() ).withFragmentIdentifier( "bazar" );
            return outcomes().ok( reverseRoute.httpUrl() ).build();
        }

    }

    @ClassRule
    public static final QiWebRule QIWEB = new QiWebRule( new RoutesParserProvider(
        "GET /simpleMethod org.qiweb.runtime.routes.ReverseRoutesTest$Controller.simpleMethod\n"
        + "GET /simpleMethod/:param/foo org.qiweb.runtime.routes.ReverseRoutesTest$Controller.simpleMethod( String param )\n"
        + "GET /wild/*card org.qiweb.runtime.routes.ReverseRoutesTest$Controller.wild( String card )\n"
        + "GET /query/:path/string org.qiweb.runtime.routes.ReverseRoutesTest$Controller.qstring( String path, String qsOne, String qsTwo )\n"
        + "GET /appended/qs org.qiweb.runtime.routes.ReverseRoutesTest$Controller.appendedQueryString\n"
        + "GET /fragment/identifier org.qiweb.runtime.routes.ReverseRoutesTest$Controller.fragmentIdentifier" ) );

    @Test
    public void testSimpleMethod()
        throws Exception
    {
        String url = QIWEB.baseHttpUrl() + "/simpleMethod";
        expect().
            statusCode( 200 ).
            body( equalTo( url ) ).
            when().
            get( url );
    }

    @Test
    public void testSimpleMethodWithParam()
        throws Exception
    {
        String url = QIWEB.baseHttpUrl() + "/simpleMethod/test/foo";
        expect().
            statusCode( 200 ).
            body( equalTo( url ) ).
            when().
            get( url );
    }

    @Test
    public void testWildcard()
        throws Exception
    {
        String url = QIWEB.baseHttpUrl() + "/wild/wild/wild/card";
        expect().
            statusCode( 200 ).
            body( equalTo( url ) ).
            when().
            get( url );
    }

    @Test
    public void testQueryString()
        throws Exception
    {
        String url = QIWEB.baseHttpUrl() + "/query/foo/string?qsOne=bar&qsTwo=bazar";
        expect().
            statusCode( 200 ).
            body( equalTo( url ) ).
            when().
            get( url );
    }

    @Test
    public void testQueryStringWithNoValueParam()
        throws Exception
    {
        String url = QIWEB.baseHttpUrl() + "/query/foo/string?qsOne=bar&qsTwo=";
        expect().
            statusCode( 200 ).
            body( equalTo( url ) ).
            when().
            get( url );
    }

    @Test
    public void testQueryStringWithNoValueParamSeveralTimes()
        throws Exception
    {
        String url = QIWEB.baseHttpUrl() + "/query/foo/string?qsOne=bar&qsTwo=&qsTwo=";
        expect().
            statusCode( 200 ).
            when().
            body( equalTo( QIWEB.baseHttpUrl() + "/query/foo/string?qsOne=bar&qsTwo=" ) ).
            get( url );
    }

    @Test
    public void testQueryStringWithSameValueParamSeveralTimes()
        throws Exception
    {
        String url = QIWEB.baseHttpUrl() + "/query/foo/string?qsOne=bar&qsTwo=bar&qsTwo=bar";
        expect().
            statusCode( 200 ).
            body( equalTo( QIWEB.baseHttpUrl() + "/query/foo/string?qsOne=bar&qsTwo=bar" ) ).
            when().
            get( url );
    }

    @Test
    public void testAppendedQueryString()
        throws Exception
    {
        String url = QIWEB.baseHttpUrl() + "/appended/qs";
        given().
            queryParam( "bar", "bazar" ).
            queryParam( "foo", "bar" ).
            expect().
            statusCode( 200 ).
            body( equalTo( url + "?bar=bazar&foo=bar" ) ).
            when().
            get( url );
    }

    @Test
    public void testFragmentIdentifier()
        throws Exception
    {
        String url = QIWEB.baseHttpUrl() + "/fragment/identifier";
        expect().
            statusCode( 200 ).
            body( equalTo( url + "#bazar" ) ).
            when().
            get( url );
    }

}
