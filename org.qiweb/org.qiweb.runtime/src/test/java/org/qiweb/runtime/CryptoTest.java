/*
 * Copyright (c) 2013 the original author or authors
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
package org.qiweb.runtime;

import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.test.QiWebRule;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class CryptoTest
{
    @ClassRule
    public static final QiWebRule QIWEB = new QiWebRule();

    @Test
    public void testGenerateNewSecret()
    {
        String secret = QIWEB.application().crypto().genNew256bitsHexSecret();
        assertThat( secret.length(), equalTo( 64 ) );
    }

    @Test
    public void testSignature()
    {
        String left = QIWEB.application().crypto().hexHmacSha256( "Text to be signed." );
        String right = QIWEB.application().crypto().hexHmacSha256( "Text to be signed." );
        assertThat( left, equalTo( right ) );
    }
}
