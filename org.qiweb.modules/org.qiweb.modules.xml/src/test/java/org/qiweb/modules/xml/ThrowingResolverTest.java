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

import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.test.QiWebRule;

/**
 * Default settings XML Test.
 */
public class ThrowingResolverTest
    extends TestBase
{
    @ClassRule
    public static final QiWebRule QIWEB = new QiWebRule();

    @Override
    protected XML xml()
    {
        return QIWEB.application().plugin( XML.class );
    }

    // DTD resolution blocked
    @Test( expected = UncheckedXMLException.class )
    @Override
    public void dtdPublic_DOM()
    {
        super.dtdPublic_DOM();
    }

    // DTD resolution blocked
    @Test( expected = UncheckedXMLException.class )
    @Override
    public void dtdPublicURL_DOM()
    {
        super.dtdPublicURL_DOM();
    }

    // DTD resolution blocked
    @Test( expected = UncheckedXMLException.class )
    @Override
    public void dtdSystem_DOM()
    {
        super.dtdSystem_DOM();
    }

    // DTD resolution blocked
    @Test( expected = UncheckedXMLException.class )
    @Override
    public void dtdSystemURL_DOM()
    {
        super.dtdSystemURL_DOM();
    }

    // DTD resolution blocked
    @Test( expected = UncheckedXMLException.class )
    @Override
    public void externalEntityRegexp3_DOM()
    {
        super.externalEntityRegexp3_DOM();
    }
}
