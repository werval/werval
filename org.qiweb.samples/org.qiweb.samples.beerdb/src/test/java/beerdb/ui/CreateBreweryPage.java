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

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.fluentlenium.FluentLeniumAssertions.assertThat;
import static org.qiweb.api.exceptions.IllegalArguments.ensureNotEmpty;

/**
 * Create Brewery Page Object.
 */
public class CreateBreweryPage
    extends FluentPage
{
    private final String url;

    public CreateBreweryPage( WebDriver driver, String url )
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
        assertThat( findFirst( "#new-brewery" ) ).isNotNull();
    }

    public void fillName( String name )
    {
        fill( "#brewery-name" ).with( name );
        assertThat( findFirst( "#brewery-name" ).getValue() ).isEqualTo( name );
    }

    public void fillUrl( String url )
    {
        fill( "#brewery-url" ).with( url );
        assertThat( findFirst( "#brewery-url" ).getValue() ).isEqualTo( url );
    }

    public void fillDescription( String description )
    {
        fill( "#brewery-description" ).with( description );
        assertThat( findFirst( "#brewery-description" ).getValue() ).isEqualTo( description );
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
