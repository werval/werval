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
import org.fluentlenium.core.domain.FluentWebElement;
import org.openqa.selenium.WebDriver;
import org.qiweb.api.util.Strings;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.fluentlenium.FluentLeniumAssertions.assertThat;
import static org.qiweb.api.exceptions.IllegalArguments.ensureNotEmpty;

/**
 * Breweries Page Object.
 */
public class BreweriesPage
    extends FluentPage
{
    private final String url;

    public BreweriesPage( WebDriver driver, String url )
    {
        super( driver );
        ensureNotEmpty( "url", url );
        this.url = url;
    }

    @Override
    public String getUrl()
    {
        return url;
    }

    @Override
    public void isAt()
    {
        assertThat( findFirst( "ul.navbar-nav li" ).getAttribute( "class" ) ).isEqualTo( "active" );
        assertThat( findFirst( "#breweries" ) ).isNotNull();
    }

    public long totalCount()
    {
        String displayCountText = findFirst( ".page-header h2 small" ).getText().trim();
        return Integer.valueOf( displayCountText.substring( 0, displayCountText.indexOf( ' ' ) ) );
    }

    public long listCount()
    {
        return find( ".list-group a" ).size();
    }

    public void fillFilterForm( String filter )
    {
        fill( "#search" ).with( filter );
    }

    public void clearFilterForm()
    {
        fill( "#search" ).with( Strings.EMPTY );
    }

    public BreweryPage clickBrewery( int index )
    {
        FluentWebElement link = find( ".list-group a" ).get( index );
        String href = link.getAttribute( "href" );
        link.click();
        return new BreweryPage( getDriver(), href );
    }

    public CreateBreweryPage createBrewery()
    {
        FluentWebElement link = findFirst( ".add-button" );
        String href = link.getAttribute( "href" );
        link.click();
        return new CreateBreweryPage( getDriver(), href );
    }
}
