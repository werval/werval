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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.werval.util.IdentityGenerator;
import io.werval.util.UUIDIdentityGenerator;

import org.thymeleaf.TemplateProcessingParameters;
import org.thymeleaf.resourceresolver.IResourceResolver;
import org.thymeleaf.util.Validate;

import static io.werval.util.Charsets.UTF_8;

/**
 * Thymeleaf TemplateResolver for named templates.
 */
/* package */ class StringResourceResolver
    implements IResourceResolver
{
    private static final String NAME = "WERVAL_STRING";
    private final IdentityGenerator stringTemplateIdGen = new UUIDIdentityGenerator();
    private final Map<String, String> templates = new ConcurrentHashMap<>();

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public InputStream getResourceAsStream( TemplateProcessingParameters processingParams, String resourceName )
    {
        Validate.notNull( resourceName, "Resource name cannot be null" );
        String templateContent = templates.get( resourceName );
        if( templateContent == null )
        {
            return null;
        }
        return new ByteArrayInputStream( templateContent.getBytes( UTF_8 ) );
    }

    /* package */ String registerStringTemplate( String templateContent )
    {
        String identity = stringTemplateIdGen.newIdentity();
        templates.put( identity, templateContent );
        return identity;
    }

    /* package */ void unregister( String templateName )
    {
        templates.remove( templateName );
    }
}
