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

import org.qiweb.api.Application;
import org.qiweb.api.Config;
import org.qiweb.api.exceptions.ActivationException;
import org.qiweb.api.templates.Templates;
import org.qiweb.api.templates.TemplatesPlugin;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.TemplateResolver;

import static java.util.Locale.US;
import static org.qiweb.api.Mode.DEV;
import static org.qiweb.util.Strings.EMPTY;
import static org.qiweb.util.Strings.withTrail;
import static org.qiweb.util.Strings.withoutHead;

/**
 * Thymeleaf Templates Plugin.
 */
public class ThymeleafPlugin
    extends TemplatesPlugin
{
    private ThymeleafTemplates templates;

    @Override
    public Templates api()
    {
        return templates;
    }

    @Override
    public void onActivate( Application application )
        throws ActivationException
    {
        // Load configuration
        Config config = application.config().object( "thymeleaf" );
        String templateMode = config.string( "template_mode" );
        String characterEncoding = config.has( "charset" )
                                   ? config.string( "charset" ).toUpperCase( US )
                                   : application.defaultCharset().name();
        String prefix = withTrail( withoutHead( config.string( "base_path" ), "/" ), "/" );
        String suffix = config.has( "suffix" )
                        ? config.string( "suffix" )
                        : EMPTY;

        // Setup named templates resolver
        TemplateResolver namedResolver = new TemplateResolver();
        namedResolver.setTemplateMode( templateMode );
        namedResolver.setCharacterEncoding( characterEncoding );
        namedResolver.setPrefix( prefix );
        namedResolver.setSuffix( suffix );
        if( application.mode() == DEV )
        {
            namedResolver.setCacheable( false );
        }
        else if( config.has( "cache_ttl" ) )
        {
            namedResolver.setCacheTTLMs( config.milliseconds( "cache_ttl" ) );
        }
        namedResolver.setResourceResolver( new NamedResourceResolver( application.classLoader() ) );

        // Setup string templates resolver
        TemplateResolver stringResolver = new TemplateResolver();
        stringResolver.setTemplateMode( templateMode );
        stringResolver.setCharacterEncoding( characterEncoding );
        stringResolver.setCacheable( false );
        StringResourceResolver stringResourceResolver = new StringResourceResolver();
        stringResolver.setResourceResolver( stringResourceResolver );

        // Create Thymeleaf Engine
        TemplateEngine thymeleaf = new TemplateEngine();
        thymeleaf.addTemplateResolver( namedResolver );
        thymeleaf.addTemplateResolver( stringResolver );

        // Done!
        templates = new ThymeleafTemplates( thymeleaf, stringResourceResolver );
    }

    @Override
    public void onPassivate( Application application )
    {
        templates.shutdown();
        templates = null;
    }
}
