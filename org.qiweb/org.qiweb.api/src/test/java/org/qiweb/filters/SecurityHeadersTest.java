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
package org.qiweb.filters;

import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.test.QiWebHttpRule;

import static com.jayway.restassured.RestAssured.expect;
import static org.qiweb.api.context.CurrentContext.outcomes;

/**
 * Security Headers Test.
 */
public class SecurityHeadersTest
{
    @ClassRule
    public static final QiWebHttpRule QIWEB = new QiWebHttpRule( new RoutesParserProvider(
        "GET /frameOptions org.qiweb.filters.SecurityHeadersTest$Controller.frameOptions\n"
        + "GET /frameOptionsConfig org.qiweb.filters.SecurityHeadersTest$Controller.frameOptionsConfig\n"
        + "GET /xssProtection org.qiweb.filters.SecurityHeadersTest$Controller.xssProtection\n"
        + "GET /xssProtectionConfig org.qiweb.filters.SecurityHeadersTest$Controller.xssProtectionConfig\n"
        + "GET /contentTypeOptions org.qiweb.filters.SecurityHeadersTest$Controller.contentTypeOptions\n"
        + "GET /hsts org.qiweb.filters.SecurityHeadersTest$Controller.hsts\n"
        + "GET /hstsConfig org.qiweb.filters.SecurityHeadersTest$Controller.hstsConfig\n"
    ) );

    public static class Controller
    {
        @XFrameOptions
        public Outcome frameOptions()
        {
            return outcomes().ok().build();
        }

        @XFrameOptions( "SAMEORIGIN" )
        public Outcome frameOptionsConfig()
        {
            return outcomes().ok().build();
        }

        @XXSSProtection
        public Outcome xssProtection()
        {
            return outcomes().ok().build();
        }

        @XXSSProtection( "1" )
        public Outcome xssProtectionConfig()
        {
            return outcomes().ok().build();
        }

        @XContentTypeOptions
        public Outcome contentTypeOptions()
        {
            return outcomes().ok().build();
        }

        @HSTS
        public Outcome hsts()
        {
            return outcomes().ok().build();
        }

        @HSTS( maxAge = 500, includeSubDomains = true )
        public Outcome hstsConfig()
        {
            return outcomes().ok().build();
        }
    }

    @Test
    public void frameOptions()
    {
        expect()
            .statusCode( 200 )
            .header( "X-Frame-Options", "DENY" )
            .when()
            .get( "/frameOptions" );
    }

    @Test
    public void frameOptionsConfig()
    {
        expect()
            .statusCode( 200 )
            .header( "X-Frame-Options", "SAMEORIGIN" )
            .when()
            .get( "/frameOptionsConfig" );
    }

    @Test
    public void xssProtection()
    {
        expect()
            .statusCode( 200 )
            .header( "X-XSS-Protection", "1; mode=block" )
            .when()
            .get( "/xssProtection" );
    }

    @Test
    public void xssProtectionConfig()
    {
        expect()
            .statusCode( 200 )
            .header( "X-XSS-Protection", "1" )
            .when()
            .get( "/xssProtectionConfig" );
    }

    @Test
    public void contentTypeOptions()
    {
        expect()
            .statusCode( 200 )
            .header( "X-Content-Type-Options", "nosniff" )
            .when()
            .get( "/contentTypeOptions" );
    }

    @Test
    public void hsts()
    {
        expect()
            .statusCode( 200 )
            .header( "Strict-Transport-Security", "max-age=480" )
            .when()
            .get( "/hsts" );
    }

    @Test
    public void hstsConfig()
    {
        expect()
            .statusCode( 200 )
            .header( "Strict-Transport-Security", "max-age=500; includeSubDomains" )
            .when()
            .get( "/hstsConfig" );
    }
}
