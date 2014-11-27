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
package org.qiweb.test.templates;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import org.qiweb.api.templates.Template;
import org.qiweb.api.templates.Templates;
import org.qiweb.test.QiWebTest;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.qiweb.util.Maps.fromMap;

/**
 * Templates Test.
 *
 * @navassoc 1 test * Templates
 */
public abstract class TemplatesTest
    extends QiWebTest
{
    protected abstract String templateName();

    protected abstract String templateContent();

    @Test
    public void namedTemplate()
    {
        String templateName = templateName();

        String name = newName();
        Map<String, Object> context = contextFor( name );

        Template template = application().plugin( Templates.class ).named( templateName );

        String output = template.render( context );

        assertThat( output, containsString( "<title>Hello " + name + "!</title>" ) );
        assertThat( output, containsString( "<h1>Hello " + name + "!</h1>" ) );
    }

    @Test
    public void stringTemplate()
    {
        String templateContent = templateContent();

        String name = newName();
        Map<String, Object> context = contextFor( name );

        Template template = application().plugin( Templates.class ).of( templateContent );

        String output = template.render( context );

        assertThat( output, containsString( "<title>Hello " + name + "!</title>" ) );
        assertThat( output, containsString( "<h1>Hello " + name + "!</h1>" ) );
    }

    private String newName()
    {
        return UUID.randomUUID().toString();
    }

    private Map<String, Object> contextFor( String name )
    {
        return fromMap( new LinkedHashMap<String, Object>() )
            .put( "name", name )
            .toMap();
    }
}
