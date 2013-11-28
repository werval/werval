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
package beerdb.ui;

import org.fluentlenium.core.FluentPage;
import org.openqa.selenium.WebDriver;
import org.qiweb.api.util.Strings;
import org.qiweb.test.QiWebTestSupport;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.fluentlenium.FluentLeniumAssertions.assertThat;

/**
 * Breweries Page Object.
 */
public class BeersPage
    extends FluentPage
{

    private final QiWebTestSupport qiweb;

    public BeersPage( WebDriver driver, QiWebTestSupport qiweb )
    {
        super( driver );
        assertThat( qiweb ).isNotNull();
        this.qiweb = qiweb;
    }

    @Override
    public String getUrl()
    {
        return qiweb.baseHttpUrl() + "/#/beers";
    }

    @Override
    public void isAt()
    {
        assertThat( title() ).
            isEqualTo( "Beer Database - QiWeb Sample App" );
        assertThat( find( "ul.navbar-nav li" ).get( 1 ).getAttribute( "class" ) ).
            isEqualTo( "active" );
        assertThat( findFirst( "#beers" ) ).
            isNotNull();
    }

    public int totalCount()
    {
        return find( ".list-group a" ).size();
    }

    public long visibleCount()
    {
        return find( ".list-group a" ).stream().filter( each -> each.isDisplayed() ).count();
    }

    public void fillFilterForm( String filter )
    {
        fill( "#search" ).with( filter );
    }

    public void clearFilterForm()
    {
        fill( "#search" ).with( Strings.EMPTY );
    }

}
