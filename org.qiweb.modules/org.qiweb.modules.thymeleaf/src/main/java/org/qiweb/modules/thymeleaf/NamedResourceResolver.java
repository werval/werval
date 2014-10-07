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

import java.io.InputStream;
import org.thymeleaf.TemplateProcessingParameters;
import org.thymeleaf.resourceresolver.IResourceResolver;
import org.thymeleaf.util.Validate;

/**
 * Thymeleaf TemplateResolver for named templates.
 */
/* package */ class NamedResourceResolver
    implements IResourceResolver
{
    private static final String NAME = "QIWEB_NAMED";
    private final ClassLoader loader;

    /* package */ NamedResourceResolver( ClassLoader loader )
    {
        this.loader = loader;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public InputStream getResourceAsStream( TemplateProcessingParameters templateProcessingParameters, String resourceName )
    {
        Validate.notNull( resourceName, "Resource name cannot be null" );
        return loader.getResourceAsStream( resourceName );
    }
}
