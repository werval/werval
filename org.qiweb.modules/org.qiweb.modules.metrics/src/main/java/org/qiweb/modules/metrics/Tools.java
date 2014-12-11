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
package org.qiweb.modules.metrics;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.jvm.ThreadDump;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.werval.api.outcomes.Outcome;
import io.werval.controllers.Classpath;
import io.werval.filters.ContentSecurityPolicy;
import io.werval.filters.NeverCached;
import io.werval.filters.XContentTypeOptions;
import io.werval.filters.XFrameOptions;
import io.werval.filters.XXSSProtection;
import java.io.ByteArrayOutputStream;
import java.lang.management.ManagementFactory;
import java.util.SortedMap;
import org.qiweb.modules.json.JSON;

import static io.werval.api.context.CurrentContext.outcomes;
import static io.werval.api.context.CurrentContext.plugin;
import static io.werval.api.context.CurrentContext.request;

/**
 * Metrics Tools.
 */
@NeverCached
@XContentTypeOptions
public class Tools
{
    @ContentSecurityPolicy
    @XFrameOptions
    @XXSSProtection
    public Outcome devShellIndex()
    {
        return new Classpath().resource( "org/qiweb/modules/metrics/devshell-index.html" );
    }

    public Outcome metrics()
        throws JsonProcessingException
    {
        return outcomes().ok( getJsonWriter().writeValueAsBytes( plugin( Metrics.class ).metrics() ) ).asJson().build();
    }

    public Outcome healthchecks()
        throws JsonProcessingException
    {
        SortedMap<String, HealthCheck.Result> results = plugin( Metrics.class ).healthChecks().runHealthChecks();
        byte[] body = getJsonWriter().writeValueAsBytes( results );
        if( !results.values().stream().allMatch( r -> r.isHealthy() ) )
        {
            return outcomes().internalServerError().withBody( body ).asJson().build();
        }
        return outcomes().ok( body ).asJson().build();
    }

    public Outcome threadDump()
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new ThreadDump( ManagementFactory.getThreadMXBean() ).dump( baos );
        return outcomes().ok( baos.toByteArray() ).asTextPlain().build();
    }

    private static ObjectWriter getJsonWriter()
    {
        final boolean prettyPrint = Boolean.parseBoolean( request().queryString().firstValue( "pretty" ) );
        if( prettyPrint )
        {
            return plugin( JSON.class ).mapper().writerWithDefaultPrettyPrinter();
        }
        return plugin( JSON.class ).mapper().writer();
    }
}
