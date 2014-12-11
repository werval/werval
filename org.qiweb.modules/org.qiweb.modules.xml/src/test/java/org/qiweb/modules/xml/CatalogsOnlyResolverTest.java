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
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Catalogs-Only Resolver Test.
 */
public class CatalogsOnlyResolverTest
    extends TestBase
{
    @ClassRule
    public static final QiWebRule QIWEB = new QiWebRule( "catalogs-only.conf" );

    @Override
    protected XML xml()
    {
        return QIWEB.application().plugin( XML.class );
    }

    @Ignore( "DoS - OutOfMemoryError !!!" )
    @Test
    @Override
    public void externalEntityRegexp3_DOM()
    {
        super.externalEntityRegexp3_DOM();
    }
}
