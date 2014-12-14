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
package io.werval.modules.jpa.internal;

import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.sessions.Session;
import io.werval.modules.metrics.Metrics;

/**
 * EclipseLink Metrics SessionCustomizer.
 */
public class MetricsSessionCustomizer
    implements SessionCustomizer
{
    public static Metrics metricsHack;
    private final Metrics metrics;

    public MetricsSessionCustomizer()
    {
        this.metrics = metricsHack;
    }

    @Override
    public void customize( Session session )
        throws Exception
    {
        session.setProfiler( new MetricsSessionProfiler( metrics ) );
    }
}