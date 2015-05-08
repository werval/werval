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
package io.werval.doc;

import java.io.IOException;
import java.nio.CharBuffer;

import org.sitemesh.builder.SiteMeshOfflineBuilder;
import org.sitemesh.offline.SiteMeshOffline;
import org.sitemesh.offline.directory.Directory;
import org.sitemesh.offline.directory.InMemoryDirectory;

import static io.werval.util.Charsets.UTF_8;
import static io.werval.util.InputStreams.BUF_SIZE_4K;
import static io.werval.util.InputStreams.readAllAsString;

/**
 * SiteMesh Helper.
 *
 * Use SiteMesh to decorate HTML pages.
 * <p>
 * Damn simple, pretty slow, but this is not production code so ... optimize only if needed.
 */
/* package */ final class SiteMeshHelper
{
    /* package */ static String decorate( String path, String html )
        throws IOException
    {
        Directory source = new InMemoryDirectory( UTF_8 );
        Directory destination = new InMemoryDirectory( UTF_8 );

        String decorator = readAllAsString(
            SiteMeshHelper.class.getResourceAsStream( "decorator.html" ),
            BUF_SIZE_4K,
            UTF_8
        );
        source.save( "decorator.html", CharBuffer.wrap( decorator.toCharArray() ) );

        SiteMeshOffline sitemesh = new SiteMeshOfflineBuilder()
            .setSourceDirectory( source )
            .setDestinationDirectory( destination )
            .addDecoratorPath( "/*", "decorator.html" )
            .create();

        CharBuffer result = sitemesh.processContent( path, CharBuffer.wrap( html.toCharArray() ) );

        return result.toString();
    }

    private SiteMeshHelper()
    {
    }
}
