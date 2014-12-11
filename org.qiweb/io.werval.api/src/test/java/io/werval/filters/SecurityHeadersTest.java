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
package io.werval.filters;

import io.werval.api.outcomes.Outcome;
import io.werval.runtime.routes.RoutesParserProvider;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.qiweb.test.QiWebHttpRule;
import org.qiweb.test.util.Slf4jRule;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;
import static io.werval.api.context.CurrentContext.outcomes;
import static io.werval.api.mime.MimeTypes.APPLICATION_JSON;
import static io.werval.api.mime.MimeTypes.APPLICATION_XML;
import static org.junit.Assert.assertTrue;

/**
 * Security Headers Test.
 */
public class SecurityHeadersTest
{
    @ClassRule
    public static final QiWebHttpRule QIWEB = new QiWebHttpRule( new RoutesParserProvider(
        "GET /frameOptions io.werval.filters.SecurityHeadersTest$Controller.frameOptions\n"
        + "GET /frameOptionsConfig io.werval.filters.SecurityHeadersTest$Controller.frameOptionsConfig\n"
        + "GET /xssProtection io.werval.filters.SecurityHeadersTest$Controller.xssProtection\n"
        + "GET /xssProtectionConfig io.werval.filters.SecurityHeadersTest$Controller.xssProtectionConfig\n"
        + "GET /contentTypeOptions io.werval.filters.SecurityHeadersTest$Controller.contentTypeOptions\n"
        + "GET /hsts io.werval.filters.SecurityHeadersTest$Controller.hsts\n"
        + "GET /hstsConfig io.werval.filters.SecurityHeadersTest$Controller.hstsConfig\n"
        + "GET /csp io.werval.filters.SecurityHeadersTest$Controller.csp\n"
        + "GET /cspConfig io.werval.filters.SecurityHeadersTest$Controller.cspConfig\n"
        + "GET /cspRepeat io.werval.filters.SecurityHeadersTest$Controller.cspRepeat\n"
        + "POST /cspViolations io.werval.filters.ContentSecurityPolicy$ViolationLogger.logViolation\n"
        + "GET /dnt io.werval.filters.SecurityHeadersTest$Controller.dnt\n"
        + "GET /dntConfig io.werval.filters.SecurityHeadersTest$Controller.dntConfig\n"
        + "POST /acceptContentTypes io.werval.filters.SecurityHeadersTest$Controller.acceptContentTypes\n"
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

        @ContentSecurityPolicy
        public Outcome csp()
        {
            return outcomes().ok().build();
        }

        @ContentSecurityPolicy( policy = "default-src 'self'; report-uri /cspViolations", reportOnly = true )
        public Outcome cspConfig()
        {
            return outcomes().ok().build();
        }

        @ContentSecurityPolicy
        @ContentSecurityPolicy( policy = "default-src 'self'; report-uri /cspViolations", reportOnly = true )
        public Outcome cspRepeat()
        {
            return outcomes().ok().build();
        }

        @DoNotTrack
        public Outcome dnt()
        {
            return outcomes().ok().build();
        }

        @DoNotTrack( optIn = true )
        public Outcome dntConfig()
        {
            return outcomes().ok().build();
        }

        @AcceptContentTypes( APPLICATION_JSON )
        public Outcome acceptContentTypes()
        {
            return outcomes().ok().build();
        }
    }

    @Rule
    public final Slf4jRule slf4j = new Slf4jRule()
    {
        {
            record( Level.WARN );
            recordForName( ContentSecurityPolicy.ViolationLogger.class.getName() );
        }
    };

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

    @Test
    public void csp()
    {
        expect()
            .statusCode( 200 )
            .header( "Content-Security-Policy", "default-src 'self'" )
            .header( "X-Content-Security-Policy", "default-src 'self'" )
            .header( "X-WebKit-CSP", "default-src 'self'" )
            .when()
            .get( "/csp" );
    }

    @Test
    public void cspConfig()
    {
        expect()
            .statusCode( 200 )
            .header( "Content-Security-Policy-Report-Only", "default-src 'self'; report-uri /cspViolations" )
            .header( "X-Content-Security-Policy-Report-Only", "default-src 'self'; report-uri /cspViolations" )
            .header( "X-WebKit-CSP-Report-Only", "default-src 'self'; report-uri /cspViolations" )
            .when()
            .get( "/cspConfig" );
    }

    @Test
    public void cspRepeat()
    {
        expect()
            .statusCode( 200 )
            .header( "Content-Security-Policy", "default-src 'self'" )
            .header( "X-Content-Security-Policy", "default-src 'self'" )
            .header( "X-WebKit-CSP", "default-src 'self'" )
            .header( "Content-Security-Policy-Report-Only", "default-src 'self'; report-uri /cspViolations" )
            .header( "X-Content-Security-Policy-Report-Only", "default-src 'self'; report-uri /cspViolations" )
            .header( "X-WebKit-CSP-Report-Only", "default-src 'self'; report-uri /cspViolations" )
            .when()
            .get( "/cspRepeat" );
    }

    @Test
    public void cspViolations()
    {
        String violationReport = "{\n"
                                 + "  \"csp-report\": {\n"
                                 + "    \"document-uri\": \"http://example.org/page.html\",\n"
                                 + "    \"referrer\": \"http://evil.example.com/haxor.html\",\n"
                                 + "    \"blocked-uri\": \"http://evil.example.com/image.png\",\n"
                                 + "    \"violated-directive\": \"default-src 'self'\",\n"
                                 + "    \"original-policy\": \"default-src 'self'; report-uri /cspViolations\"\n"
                                 + "  }\n"
                                 + "}";
        given()
            .contentType( APPLICATION_JSON )
            .body( violationReport )
            .expect()
            .statusCode( 204 )
            .when()
            .post( "/cspViolations" );
        assertTrue( slf4j.contains( violationReport ) );
    }

    @Test
    public void dnt()
    {
        expect()
            .statusCode( 200 )
            .header( "DNT", "1" )
            .when()
            .get( "/dnt" );
    }

    @Test
    public void dntConfig()
    {
        expect()
            .statusCode( 200 )
            .header( "DNT", "0" )
            .when()
            .get( "/dntConfig" );
    }

    @Test
    public void acceptContentTypes()
    {
        given()
            .contentType( APPLICATION_JSON )
            .body( "{}" )
            .expect()
            .statusCode( 200 )
            .when()
            .post( "/acceptContentTypes" );

        given()
            .contentType( APPLICATION_XML )
            .body( "<root/>" )
            .expect()
            .statusCode( 400 )
            .when()
            .post( "/acceptContentTypes" );

        given()
            .body( "" )
            .expect()
            .statusCode( 400 )
            .when()
            .post( "/acceptContentTypes" );
    }
}
