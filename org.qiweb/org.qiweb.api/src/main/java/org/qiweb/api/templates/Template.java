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
package org.qiweb.api.templates;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import org.qiweb.api.exceptions.TemplateException;

import static java.util.Collections.EMPTY_MAP;

/**
 * Template, ready to render.
 */
public interface Template
{
    /**
     * Render a Template as String without applying a context.
     *
     * @return The rendered Template
     *
     * @throws TemplateException when something goes wrong rendering the template
     */
    default String render()
        throws TemplateException
    {
        return render( EMPTY_MAP );
    }

    /**
     * Render a Template as String applying a given context.
     *
     * @param context The context to apply
     *
     * @return The rendered Template
     *
     * @throws TemplateException when something goes wrong rendering the template
     */
    default String render( Map<String, Object> context )
        throws TemplateException
    {
        StringWriter writer = new StringWriter();
        render( context, writer );
        return writer.toString();
    }

    /**
     * Render a Template to the given output without applying a context.
     *
     * @param output The output Writer
     *
     * @throws TemplateException when something goes wrong rendering the template
     */
    default void render( Writer output )
        throws TemplateException
    {
        render( EMPTY_MAP, output );
    }

    /**
     * Render a Template to the given output applying a given context.
     *
     * @param context The context to apply
     * @param output  The output Writer
     *
     * @throws TemplateException when something goes wrong rendering the template
     */
    void render( Map<String, Object> context, Writer output )
        throws TemplateException;
}
