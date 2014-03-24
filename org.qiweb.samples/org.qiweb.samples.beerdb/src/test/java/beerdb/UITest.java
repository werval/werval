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

import beerdb.ui.BeerPage;
import beerdb.ui.BeersPage;
import beerdb.ui.BreweriesPage;
import beerdb.ui.BreweryPage;
import beerdb.ui.CreateBeerPage;
import beerdb.ui.CreateBreweryPage;
import beerdb.ui.EditBeerPage;
import beerdb.ui.EditBreweryPage;
import org.fluentlenium.adapter.FluentTest;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.test.QiWebHttpRule;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.fluentlenium.FluentLeniumAssertions.assertThat;

public class UITest
    extends FluentTest
{
    @ClassRule
    public static final QiWebHttpRule QIWEB = new QiWebHttpRule();

    @Override
    public String getDefaultBaseUrl()
    {
        return QIWEB.baseHttpUrl();
    }

    private BreweriesPage breweriesPage;
    private BeersPage beersPage;

    @Before
    public void createPages()
    {
        breweriesPage = new BreweriesPage( getDriver(), getDefaultBaseUrl() + "/#/breweries" );
        beersPage = new BeersPage( getDriver(), getDefaultBaseUrl() + "/#/beers" );
    }

    @Test
    public void test()
    {
        BreweryPage breweryPage;
        CreateBreweryPage createBreweryPage;
        EditBreweryPage editBreweryPage;
        BeerPage beerPage;
        CreateBeerPage createBeerPage;
        EditBeerPage editBeerPage;

        // Start from Breweries
        //
        goTo( "/" );
        assertThat( breweriesPage ).isAt();
        assertThat( breweriesPage.totalCount() ).isEqualTo( 2 );
        assertThat( breweriesPage.listCount() ).isEqualTo( 2 );

        // Click on first brewery and take a look at it
        //
        goTo( breweriesPage );
        assertThat( breweriesPage ).isAt();
        breweryPage = breweriesPage.clickBrewery( 0 );
        assertThat( breweryPage ).isAt();

        // Click on first beer and take a look at it
        //
        beerPage = breweryPage.clickBeer( 0 );
        assertThat( beerPage ).isAt();

        // Navigate to breweries and click create brewery button, then cancel
        //
        goTo( breweriesPage );
        assertThat( breweriesPage ).isAt();
        createBreweryPage = breweriesPage.createBrewery();
        assertThat( createBreweryPage ).isAt();
        createBreweryPage.cancel();
        assertThat( breweriesPage ).isAt();

        // Click create brewery button, then click save without filling the form
        //
        createBreweryPage = breweriesPage.createBrewery();
        assertThat( createBreweryPage ).isAt();
        createBreweryPage.save();
        assertThat( createBreweryPage ).isAt();

        // Fill create brewery form and click save button
        //
        createBreweryPage.fillName( "Test Brewery" );
        createBreweryPage.fillUrl( "http://test-brewery.qiweb.org/" );
        createBreweryPage.fillDescription( "This is Test Brewery" );
        createBreweryPage.saveAndWaitForRedirect();

        // Take a look at the newly created brewery
        //
        breweryPage = new BreweryPage( getDriver(), getDriver().getCurrentUrl() );
        assertThat( breweryPage ).isAt();
        assertThat( breweryPage.breweryName() ).isEqualTo( "Test Brewery" );

        // Click edit brewery button then cancel
        //
        editBreweryPage = breweryPage.edit();
        assertThat( editBreweryPage ).isAt();
        editBreweryPage.cancel();
        assertThat( breweryPage ).isAt();

        // Click edit brewery button, clear url, try to save
        //
        editBreweryPage = breweryPage.edit();
        assertThat( editBreweryPage ).isAt();
        editBreweryPage.clearUrl();
        editBreweryPage.save();
        assertThat( editBreweryPage ).isAt();

        // Put url back, change name, then save
        //
        editBreweryPage.fillName( "Test EDITED Brewery" );
        editBreweryPage.fillUrl( "http://test-brewery.qiweb.org/" );
        editBreweryPage.saveAndWaitForRedirect();

        // Take a look at the edited brewery
        //
        goTo( breweryPage );
        assertThat( breweryPage ).isAt();
        assertThat( breweryPage.breweryName() ).isEqualTo( "Test EDITED Brewery" );

        // Navigate to breweries to see the newly created brewery in the list
        //
        goTo( breweriesPage );
        assertThat( breweriesPage ).isAt();
        assertThat( breweriesPage.listCount() ).isEqualTo( 3 );

        // Navigate to the newly created brewery and click delete button
        //
        goTo( breweryPage );
        assertThat( breweryPage ).isAt();
        breweryPage.delete();

        // Navigate to breweries to see that the newly created brewery was removed from the list
        //
        goTo( breweriesPage );
        assertThat( breweriesPage ).isAt();
        assertThat( breweriesPage.listCount() ).isEqualTo( 2 );

        // Navigate to beers
        //
        goTo( beersPage );
        assertThat( beersPage ).isAt();
        assertThat( beersPage.listCount() ).isEqualTo( 11 );

        // Click on first beer and take a look at it
        //
        beerPage = beersPage.clickBeer( 0 );
        assertThat( beerPage ).isAt();

        // Click on the beer's brewery and take a look at it
        //
        breweryPage = beerPage.navigateToBrewery();
        assertThat( breweryPage ).isAt();

        // Navigate to beers and click create beer button, then cancel
        //
        goTo( beersPage );
        assertThat( beersPage ).isAt();
        createBeerPage = beersPage.createBeer();
        createBeerPage.cancel();
        assertThat( beersPage ).isAt();

        // Click create beer button, then click save without filling the form
        //
        createBeerPage = beersPage.createBeer();
        assertThat( createBeerPage ).isAt();
        createBeerPage.save();
        assertThat( createBeerPage ).isAt();

        // Fill create beer form and click save button
        //
        createBeerPage.selectBreweryByName( "Duyck" );
        createBeerPage.fillName( "Jeanlain Bière de Bourrin" );
        createBeerPage.fillAbv( 5.4F );
        createBeerPage.fillDescription( "Good ol'times" );
        createBeerPage.saveAndWaitForRedirect();

        // Take a look at the newly created beer
        //
        beerPage = new BeerPage( getDriver(), getDriver().getCurrentUrl() );
        assertThat( beerPage ).isAt();
        assertThat( beerPage.beerName() ).isEqualTo( "Jeanlain Bière de Bourrin" );

        // Click edit beer button then cancel
        //
        editBeerPage = beerPage.edit();
        assertThat( editBeerPage ).isAt();
        editBeerPage.cancel();
        assertThat( beerPage ).isAt();

        // Click edit beer button, clear abv, try to save
        //
        editBeerPage = beerPage.edit();
        assertThat( editBeerPage ).isAt();
        editBeerPage.clearAbv();
        editBeerPage.save();
        assertThat( editBeerPage ).isAt();

        // Put abv back, change name, then save
        //
        editBeerPage.fillName( "Test EDITED Beer" );
        editBeerPage.fillAbv( 5.4F );
        editBeerPage.saveAndWaitForRedirect();

        // Take a look at the edited brewery
        //
        goTo( beerPage );
        assertThat( beerPage ).isAt();
        assertThat( beerPage.beerName() ).isEqualTo( "Test EDITED Beer" );

        // Navigate to beers to see the newly created beer in the list
        //
        goTo( beersPage );
        assertThat( beersPage ).isAt();
        assertThat( beersPage.listCount() ).isEqualTo( 12 );

        // Navigate to the newly created beer and click delete button
        //
        goTo( beerPage );
        assertThat( beerPage ).isAt();
        beerPage.delete();

        // Navigate to beers to see that the newly created beer was removed from the list
        //
        goTo( beersPage );
        assertThat( beersPage ).isAt();
        assertThat( beersPage.listCount() ).isEqualTo( 11 );
    }
}
