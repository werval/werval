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
package org.qiweb.modules.xml;

import io.werval.test.QiWebRule;
import java.io.UncheckedIOException;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * No-Catalog Unsafe Resolver Test.
 */
//@Ignore( "Not implemented yet" )
public class NoCatalogUnsafeResolverTest
    extends TestBase
{
    @ClassRule
    public static final QiWebRule QIWEB = new QiWebRule( "no-catalog-unsafe.conf" );

    @Override
    protected XML xml()
    {
        return QIWEB.application().plugin( XML.class );
    }

    // DTD resolution failed
    @Test( expected = UncheckedIOException.class )
    @Override
    public void dtdPublic_DOM()
    {
        super.dtdPublic_DOM();
    }

    // DTD resolution failed
    @Test( expected = UncheckedIOException.class )
    @Override
    public void dtdPublicURL_DOM()
    {
        super.dtdPublicURL_DOM();
    }

    // DTD resolution failed
    @Test( expected = UncheckedIOException.class )
    @Override
    public void dtdSystem_DOM()
    {
        super.dtdSystem_DOM();
    }

    // DTD resolution failed
    @Test( expected = UncheckedIOException.class )
    @Override
    public void dtdSystemURL_DOM()
    {
        super.dtdSystemURL_DOM();
    }

    // DTD resolution failed
    @Test( expected = UncheckedIOException.class )
    @Override
    public void externalEntityPublic_DOM()
    {
        super.externalEntityPublic_DOM();
    }

    // DTD resolution failed
    @Test( expected = UncheckedIOException.class )
    @Override
    public void externalEntityPublicURL_DOM()
    {
        super.externalEntityPublicURL_DOM();
    }

    // DTD resolution failed
    @Test( expected = UncheckedIOException.class )
    @Override
    public void externalEntitySystem_DOM()
    {
        super.externalEntitySystem_DOM();
    }

    // DTD resolution failed
    @Test( expected = UncheckedIOException.class )
    @Override
    public void externalEntitySystemURL_DOM()
    {
        super.externalEntitySystemURL_DOM();
    }

    // DTD resolution failed
    @Test( expected = UncheckedIOException.class )
    @Override
    public void externalEntityRegexp3_DOM()
    {
        super.externalEntityRegexp3_DOM();
    }
}
