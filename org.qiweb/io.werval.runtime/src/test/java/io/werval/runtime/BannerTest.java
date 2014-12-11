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
package io.werval.runtime;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.qiweb.test.QiWebRule;

import static io.werval.util.Charsets.UTF_8;
import static io.werval.util.InputStreams.BUF_SIZE_4K;
import static io.werval.util.InputStreams.readAllAsString;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

/**
 * Banner Test.
 */
public class BannerTest
{
    private static PrintStream oldOut;
    private static final ByteArrayOutputStream capturedOut = new ByteArrayOutputStream();

    @BeforeClass
    public static void captureSystemOut()
    {
        oldOut = System.out;
        System.setOut( new PrintStream( capturedOut ) );
    }

    @AfterClass
    public static void restoreSystemOut()
    {
        System.setOut( oldOut );
    }

    @Rule
    public final QiWebRule QIWEB = new QiWebRule( "banner-ascii.conf" );

    @Test
    public void asciiBanner()
    {
        String banner = readAllAsString(
            getClass().getClassLoader().getResourceAsStream( "banner-ascii.txt" ),
            BUF_SIZE_4K,
            UTF_8
        );
        assertThat( capturedOut.toString(), containsString( banner ) );
    }
}
