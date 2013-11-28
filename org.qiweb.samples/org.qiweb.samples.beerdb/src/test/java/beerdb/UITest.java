/**
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
package beerdb;

import beerdb.ui.BeersPage;
import beerdb.ui.BreweriesPage;
import org.fluentlenium.adapter.FluentTest;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.test.QiWebRule;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.fluentlenium.FluentLeniumAssertions.assertThat;

public class UITest
    extends FluentTest
{

    @ClassRule
    public static final QiWebRule QIWEB = new QiWebRule();
    private BreweriesPage breweriesPage;
    private BeersPage beersPage;

    @Before
    public void setupPages()
    {
        breweriesPage = new BreweriesPage( getDriver(), QIWEB );
        beersPage = new BeersPage( getDriver(), QIWEB );
    }

    @Override
    public String getDefaultBaseUrl()
    {
        return QIWEB.baseHttpUrl();
    }

    @Test
    public void test()
    {
        goTo( "/" );
        {
            assertThat( breweriesPage ).isAt();
            assertThat( breweriesPage.displayCount() ).isEqualTo( 2 );
            assertThat( breweriesPage.totalCount() ).isEqualTo( 2 );
            assertThat( breweriesPage.visibleCount() ).isEqualTo( 2 );

            breweriesPage.fillFilterForm( "valstar" );
            assertThat( breweriesPage.visibleCount() ).isEqualTo( 0 );

            //breweriesPage.clearFilterForm();
            //assertThat( breweriesPage.visibleCount() ).isEqualTo( 2 );
        }

        goTo( beersPage );
        {
            assertThat( beersPage ).isAt();
            assertThat( beersPage.totalCount() ).isEqualTo( 11 );

            beersPage.fillFilterForm( "blonde" );
            assertThat( beersPage.visibleCount() ).isEqualTo( 2 );

            //beersPage.clearFilterForm();
            //assertThat( beersPage.listCount() ).isEqualTo( 11 );
        }
    }

}
