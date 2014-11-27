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
package org.qiweb.modules.rythm;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Map;
import org.qiweb.api.exceptions.TemplateException;
import org.qiweb.api.templates.Template;
import org.qiweb.api.templates.Templates;
import org.qiweb.modules.metrics.internal.TemplatesMetricsHandler;
import org.rythmengine.RythmEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rythm based Templates implementation.
 */
/* package */ final class RythmTemplates
    implements Templates
{
    private static final Logger LOG = LoggerFactory.getLogger( RythmTemplates.class );
    private final RythmEngine engine;
    private final TemplatesMetricsHandler metricsHandler;

    /* package */ RythmTemplates( RythmEngine engine, TemplatesMetricsHandler metricsHandler )
    {
        this.engine = engine;
        this.metricsHandler = metricsHandler;
    }

    @Override
    public Template named( String templateName )
        throws TemplateException
    {
        return new NamedTemplate( engine, metricsHandler, templateName );
    }

    @Override
    public Template of( String templateContent )
        throws TemplateException
    {
        return new StringTemplate( engine, metricsHandler, templateContent );
    }

    /* package */ void shutdown()
    {
        engine.shutdown();
    }

    private static final class NamedTemplate
        implements Template
    {
        private final RythmEngine engine;
        private final TemplatesMetricsHandler metricsHandler;
        private final String templateName;

        public NamedTemplate( RythmEngine engine, TemplatesMetricsHandler metricsHandler, String templateName )
        {
            this.engine = engine;
            this.metricsHandler = metricsHandler;
            this.templateName = templateName;
        }

        @Override
        public void render( Map<String, Object> context, Writer output )
            throws TemplateException
        {
            if( LOG.isDebugEnabled() )
            {
                LOG.debug( "{}.render( {} )", this, context );
            }
            try( TemplatesMetricsHandler.Closeable namedTimer = metricsHandler.namedRenderTimer( templateName ) )
            {
                output.write( engine.render( templateName, context ) );
            }
            catch( IOException ex )
            {
                throw new UncheckedIOException( ex );
            }
        }

        @Override
        public String toString()
        {
            return "NamedTemplate{" + "templateName=" + templateName + '}';
        }
    }

    private static final class StringTemplate
        implements Template
    {
        private final RythmEngine engine;
        private final TemplatesMetricsHandler metricsHandler;
        private final String templateContent;

        public StringTemplate( RythmEngine engine, TemplatesMetricsHandler metricsHandler, String templateContent )
        {
            this.engine = engine;
            this.metricsHandler = metricsHandler;
            this.templateContent = templateContent;
        }

        @Override
        public void render( Map<String, Object> context, Writer output )
            throws TemplateException
        {
            if( LOG.isDebugEnabled() )
            {
                LOG.debug( "{}.render( {} )", this, context );
            }
            try( TemplatesMetricsHandler.Closeable inlineTimer = metricsHandler.inlineRenderTimer() )
            {
                output.write( engine.renderString( templateContent, context ) );
            }
            catch( IOException ex )
            {
                throw new UncheckedIOException( ex );
            }
        }

        @Override
        public String toString()
        {
            return "StringTemplate{" + "templateContent=" + templateContent + '}';
        }
    }

    @Override
    public String toString()
    {
        return "RythmTemplates{" + "engine=" + engine + '}';
    }
}
