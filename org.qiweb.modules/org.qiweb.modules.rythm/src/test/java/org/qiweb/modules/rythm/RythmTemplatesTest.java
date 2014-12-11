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

import com.codahale.metrics.MetricRegistry;
import org.junit.Test;
import org.qiweb.modules.metrics.Metrics;
import org.qiweb.test.templates.TemplatesTest;

import static com.codahale.metrics.MetricRegistry.name;
import static io.werval.util.Charsets.UTF_8;
import static io.werval.util.InputStreams.BUF_SIZE_4K;
import static io.werval.util.InputStreams.readAllAsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Rythm Templates Test.
 */
public class RythmTemplatesTest
    extends TemplatesTest
{
    @Override
    protected String templateName()
    {
        return "hello.html";
    }

    @Override
    protected String templateContent()
    {
        return readAllAsString( getClass().getResourceAsStream( "/views/hello.html" ), BUF_SIZE_4K, UTF_8 );
    }

    @Test
    @Override
    public void namedTemplate()
    {
        super.namedTemplate();
        MetricRegistry metrics = application().plugin( Metrics.class ).metrics();
        assertThat( metrics.timer( "org.qiweb.modules.templates.rythm.named" ).getCount(), is( 1L ) );
        assertThat( metrics.timer( name( "org.qiweb.modules.templates.rythm.named", templateName() ) ).getCount(), is( 1L ) );
        assertThat( metrics.timer( "org.qiweb.modules.templates.rythm.inline" ).getCount(), is( 0L ) );
    }

    @Test
    @Override
    public void stringTemplate()
    {
        super.stringTemplate();
        MetricRegistry metrics = application().plugin( Metrics.class ).metrics();
        assertThat( metrics.timer( "org.qiweb.modules.templates.rythm.named" ).getCount(), is( 0L ) );
        assertThat( metrics.timer( "org.qiweb.modules.templates.rythm.inline" ).getCount(), is( 1L ) );
    }
}
