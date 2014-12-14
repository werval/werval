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
package io.werval.api.templates;

import io.werval.api.exceptions.TemplateException;

/**
 * Templates.
 *
 * @navassoc 1 create * Template
 */
public interface Templates
{
    /**
     * Get a Template by name.
     *
     * @param templateName The name of the Template
     *
     * @return The Template, ready to render
     *
     * @throws TemplateException when something goes wrong resolving the template
     */
    Template named( String templateName )
        throws TemplateException;

    /**
     * Create a Template from a String.
     *
     * @param templateContent The template content
     *
     * @return The Template, ready to render
     *
     * @throws TemplateException when something goes wrong creating the template
     */
    Template of( String templateContent )
        throws TemplateException;
}
