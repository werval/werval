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
package org.qiweb.modules.thymeleaf;

import java.io.Writer;
import java.util.Map;
import org.qiweb.api.exceptions.TemplateException;
import org.qiweb.api.templates.Template;
import org.qiweb.api.templates.Templates;
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

    /* package */ ThymeleafTemplates( TemplateEngine engine, StringResourceResolver stringTemplateResolver )
    {
        this.engine = engine;
        this.stringTemplateResolver = stringTemplateResolver;
    }

    @Override
    public Template named( String templateName )
    {
        return new NamedTemplate( templateName, engine );
    }

    @Override
    public Template of( String templateContent )
    {
        return new StringTemplate( engine, stringTemplateResolver, templateContent );
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

        private NamedTemplate( String templateName, TemplateEngine engine )
        {
            this.templateName = templateName;
            this.engine = engine;
        }

        @Override
        public void render( Map<String, Object> context, Writer output )
            throws TemplateException
        {
            Context thymeleafCtx = new Context();
            thymeleafCtx.getVariables().putAll( context );
            engine.process( templateName, thymeleafCtx, output );
        }
    }

    private static final class StringTemplate
        implements Template
    {
        private final TemplateEngine engine;
        private final StringResourceResolver stringTemplateResolver;
        private final String templateContent;

        private StringTemplate(
            TemplateEngine engine,
            StringResourceResolver stringTemplateResolver,
            String templateContent
        )
        {
            this.engine = engine;
            this.stringTemplateResolver = stringTemplateResolver;
            this.templateContent = templateContent;
        }

        @Override
        public void render( Map<String, Object> context, Writer output )
            throws TemplateException
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
