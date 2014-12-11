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
package org.qiweb.modules.cache;

import io.werval.api.cache.Cache;
import io.werval.api.outcomes.Outcome;
import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.modules.metrics.Tools;
import org.qiweb.runtime.routes.RoutesParserProvider;
import org.qiweb.test.QiWebHttpRule;

import static com.jayway.restassured.RestAssured.expect;
import static io.werval.api.context.CurrentContext.plugin;
import static io.werval.api.mime.MimeTypes.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;

/**
 * EhCache Plugin Metrics Test.
 */
public class EhCacheMetricsTest
{
    @ClassRule
    public static QiWebHttpRule QIWEB = new QiWebHttpRule( new RoutesParserProvider(
        "GET / " + Controller.class.getName() + ".interaction"
    ) );

    public static class Controller
    {
        public Outcome interaction()
            throws Exception
        {
            Cache cache = plugin( Cache.class );

            // get & miss
            cache.has( "foo" );
            cache.get( "foo" );

            // get & miss & put
            cache.getOrSetDefault( "foo", "bar" );

            // put
            cache.set( "foo", "bazar" );

            // get & hit
            cache.get( "foo" );

            // get & hit
            cache.getOrSetDefault( "foo", "bar" );

            // remove
            cache.remove( "foo" );

            // get & miss
            cache.get( "foo" );

            return new Tools().metrics();
        }
    }

    @Test
    public void ehCacheMetrics()
        throws Exception
    {
        expect()
            .statusCode( 200 )
            .contentType( APPLICATION_JSON )
            .body( "meters.'org.qiweb.modules.cache.ehcache.qiweb-cache.hits'.count", is( 2 ) )
            .body( "meters.'org.qiweb.modules.cache.ehcache.qiweb-cache.misses'.count", is( 4 ) )
            .body( "timers.'org.qiweb.modules.cache.ehcache.qiweb-cache.gets'.count", is( 6 ) )
            .body( "timers.'org.qiweb.modules.cache.ehcache.qiweb-cache.sets'.count", is( 2 ) )
            .body( "timers.'org.qiweb.modules.cache.ehcache.qiweb-cache.removes'.count", is( 1 ) )
            .when()
            .get( "/" );
    }
}
