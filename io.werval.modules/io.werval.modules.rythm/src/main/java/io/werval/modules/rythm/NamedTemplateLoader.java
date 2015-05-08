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
package io.werval.modules.rythm;

import java.net.URL;

import io.werval.api.Application;

import org.rythmengine.RythmEngine;
import org.rythmengine.extension.ICodeType;
import org.rythmengine.extension.ITemplateResourceLoader;
import org.rythmengine.resource.ITemplateResource;
import org.rythmengine.resource.ResourceLoaderBase;
import org.rythmengine.resource.TemplateResourceBase;
import org.rythmengine.utils.IO;
import org.rythmengine.utils.S;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.werval.util.Strings.withoutHead;

/**
 * Named Rythm Template Resource Loader.
 */
/* package */ class NamedTemplateLoader
    extends ResourceLoaderBase
    implements ITemplateResourceLoader
{
    private static final Logger LOG = LoggerFactory.getLogger( NamedTemplateLoader.class );
    private final Application application;
    private final String root;

    /* package */ NamedTemplateLoader( Application application, String root )
    {
        super();
        this.application = application;
        this.root = root;
    }

    @Override
    public String getResourceLoaderRoot()
    {
        return root;
    }

    @Override
    public ITemplateResource load( String path )
    {
        return new TemplateResource( path, application.classLoader(), this );
    }

    private static final class TemplateResource
        implements ITemplateResource
    {
        private final ITemplateResourceLoader loader;
        private final URL url;
        private final String key;
        private String cache;

        private TemplateResource( String path, ClassLoader classLoader, ITemplateResourceLoader templateLoader )
        {
            this.loader = templateLoader;
            // strip heading slash so path will work with classes in a JAR file
            path = withoutHead( path, "/" );
            URL u = classLoader.getResource( path );
            if( u == null )
            {
                u = classLoader.getResource( loader.getResourceLoaderRoot() + "/" + path );
            }
            url = u;
            key = path;
        }

        @Override
        public Object getKey()
        {
            return key;
        }

        @Override
        public ITemplateResourceLoader getLoader()
        {
            return loader;
        }

        @Override
        public boolean isValid()
        {
            return null != url;
        }

        @Override
        public ICodeType codeType( RythmEngine engine )
        {
            return TemplateResourceBase.getTypeOfPath( engine, S.str( getKey() ) );
        }

        @Override
        public String asTemplateContent()
        {
            if( null == cache )
            {
                LOG.debug( "Loading '{}' template from {}", key, url );
                cache = IO.readContentAsString( url );
            }
            return cache;
        }

        @Override
        public String getSuggestedClassName()
        {
            String path = key;
            int colon = path.indexOf( ":" );
            if( ++colon > 0 )
            {
                path = path.substring( colon ); // strip the driver letter from windows path and scheme from the URL
            }
            while( path.startsWith( "/" ) )
            {
                path = path.substring( 1 );
            }
            while( path.startsWith( "\\" ) )
            {
                path = path.substring( 1 );
            }
            // replace characters that are invalid in a java identifier with '_'
            return path.replaceAll( "[.\\\\/ -]", "_" );
        }

        @Override
        public boolean refresh()
        {
            return false;
        }

        @Override
        public boolean equals( Object obj )
        {
            if( obj == this )
            {
                return true;
            }
            if( null == obj )
            {
                return false;
            }
            if( ITemplateResource.class.isAssignableFrom( obj.getClass() ) )
            {
                return ( (ITemplateResource) obj ).getKey().equals( getKey() );
            }
            return false;
        }

        @Override
        public int hashCode()
        {
            return getKey().hashCode();
        }
    }
}
