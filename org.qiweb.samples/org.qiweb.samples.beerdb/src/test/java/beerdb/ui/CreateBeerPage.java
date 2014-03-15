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

import com.google.common.base.Predicate;
import org.fluentlenium.core.FluentPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.fluentlenium.FluentLeniumAssertions.assertThat;
import static org.qiweb.api.exceptions.IllegalArguments.ensureNotEmpty;

/**
 * Create Beer Page Object.
 */
public class CreateBeerPage
    extends FluentPage
{
    private final String url;

    public CreateBeerPage( WebDriver driver, String url )
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
        assertThat( find( "ul.navbar-nav li" ).get( 1 ).getAttribute( "class" ) ).isEqualTo( "active" );
        assertThat( findFirst( "#new-beer" ) ).isNotNull();
    }

    public String selectedBreweryName()
    {
        return new Select( findFirst( "#beer-brewery" ).getElement() ).getFirstSelectedOption().getText();
    }

    public void selectBreweryByName( String name )
    {
        fillSelect( "#beer-brewery" ).withText( name );
        assertThat( selectedBreweryName() ).isEqualTo( name );
    }

    public void fillName( String name )
    {
        fill( "#beer-name" ).with( name );
        assertThat( findFirst( "#beer-name" ).getValue() ).isEqualTo( name );
    }

    public void fillAbv( float abv )
    {
        fill( "#beer-abv" ).with( String.valueOf( abv ) );
        assertThat( findFirst( "#beer-abv" ).getValue() ).isEqualTo( String.valueOf( abv ) );
    }

    public void fillDescription( String description )
    {
        fill( "#beer-description" ).with( description );
        assertThat( findFirst( "#beer-description" ).getValue() ).isEqualTo( description );
    }

    public void cancel()
    {
        findFirst( ".cancel-button" ).click();
    }

    public void save()
    {
        save( false );
    }

    public void saveAndWaitForRedirect()
    {
        save( true );
    }

    @SuppressWarnings( "Convert2Lambda" )
    private void save( boolean waitForRedirect )
    {
        findFirst( ".save-button" ).click();
        if( waitForRedirect )
        {
            await().until( new Predicate<WebDriver>()
            {
                @Override
                public boolean apply( WebDriver driver )
                {
                    return !driver.getCurrentUrl().endsWith( "new" );
                }
            } );
        }
    }
}
