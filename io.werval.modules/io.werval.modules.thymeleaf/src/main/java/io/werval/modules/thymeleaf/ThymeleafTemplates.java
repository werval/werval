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
package io.werval.modules.thymeleaf;

import java.io.Writer;
import java.util.Map;

import io.werval.api.exceptions.TemplateException;
import io.werval.api.templates.Template;
import io.werval.api.templates.Templates;
import io.werval.modules.metrics.internal.TemplatesMetricsHandler;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Thymeleaf Templates Implementation.
 */
/* package */ class ThymeleafTemplates
    implements Templates
{
    private final TemplateEngine engine;
    private final StringResourceResolver stringTemplateResolver;
    private final TemplatesMetricsHandler metricsHandler;

    /* package */ ThymeleafTemplates(
        TemplateEngine engine,
        StringResourceResolver stringTemplateResolver,
        TemplatesMetricsHandler metricsHandler
    )
    {
        this.engine = engine;
        this.stringTemplateResolver = stringTemplateResolver;
        this.metricsHandler = metricsHandler;
    }

    @Override
    public Template named( String templateName )
    {
        return new NamedTemplate( templateName, engine, metricsHandler );
    }

    @Override
    public Template of( String templateContent )
    {
        return new StringTemplate( engine, stringTemplateResolver, templateContent, metricsHandler );
    }

    /* package */ void shutdown()
    {
        engine.clearTemplateCache();
    }

    private static final class NamedTemplate
        implements Template
    {
        private final TemplateEngine engine;
        private final String templateName;
        private final TemplatesMetricsHandler metricsHandler;

        private NamedTemplate( String templateName, TemplateEngine engine, TemplatesMetricsHandler metricsHandler )
        {
            this.templateName = templateName;
            this.engine = engine;
            this.metricsHandler = metricsHandler;
        }

        @Override
        public void render( Map<String, Object> context, Writer output )
            throws TemplateException
        {
            try( TemplatesMetricsHandler.Closeable namedTimer = metricsHandler.namedRenderTimer( templateName ) )
            {
                Context thymeleafCtx = new Context();
                thymeleafCtx.getVariables().putAll( context );
                engine.process( templateName, thymeleafCtx, output );
            }
        }
    }

    private static final class StringTemplate
        implements Template
    {
        private final TemplateEngine engine;
        private final StringResourceResolver stringTemplateResolver;
        private final String templateContent;
        private final TemplatesMetricsHandler metricsHandler;

        private StringTemplate(
            TemplateEngine engine,
            StringResourceResolver stringTemplateResolver,
            String templateContent,
            TemplatesMetricsHandler metricsHandler
        )
        {
            this.engine = engine;
            this.stringTemplateResolver = stringTemplateResolver;
            this.templateContent = templateContent;
            this.metricsHandler = metricsHandler;
        }

        @Override
        public void render( Map<String, Object> context, Writer output )
            throws TemplateException
        {
            try( TemplatesMetricsHandler.Closeable inlineTimer = metricsHandler.inlineRenderTimer() )
            {
                Context thymeleafCtx = new Context();
                thymeleafCtx.getVariables().putAll( context );
                String identity = stringTemplateResolver.registerStringTemplate( templateContent );
                try
                {
                    engine.process( identity, thymeleafCtx, output );
                }
                finally
                {
                    stringTemplateResolver.unregister( identity );
                }
            }
        }
    }
}
