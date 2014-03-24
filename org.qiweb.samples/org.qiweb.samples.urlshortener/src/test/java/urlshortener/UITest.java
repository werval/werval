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
package urlshortener;

import com.google.common.base.Predicate;
import org.fluentlenium.adapter.FluentTest;
import org.junit.Rule;
import org.junit.Test;
import org.qiweb.test.QiWebHttpRule;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Assert UI behaviour.
 */
public class UITest
    extends FluentTest
{
    @Rule
    public QiWebHttpRule qiwebRule = new QiWebHttpRule();

    @Test
    public void testUI()
    {
        // First load
        goTo( qiwebRule.baseHttpUrl() + "/" );
        assertThat( title(), equalTo( "URL Shortener - QiWeb Sample App" ) );
        await().until( "#api-output pre" ).hasSize( 1 );
        assertThat( findFirst( "#api-output pre" ).getText(), equalTo( "[]" ) );

        String url = qiwebRule.baseHttpUrl() + "/client/lib/webjars/bootstrap/3.0.3/css/bootstrap.min.css";

        // Shorten
        fill( "#input-shorten" ).with( url );
        submit( "#form-shorten" );
        await().until( "#list-output div.panel-body" ).hasSize( 1 );
        assertThat( findFirst( "#list-output div.panel-body" ).getText(), containsString( url ) );

        // Gather hash
        String hash = findFirst( "#api-output pre" ).getText();
        hash = hash.substring( hash.lastIndexOf( '/' ) + 1, hash.length() - 3 );

        // Expand
        click( findFirst( "#list-output div.panel-heading div.pull-right a" ) );
        await().until( "#api-output pre" ).hasSize( 4 );
        assertThat( findFirst( "#api-output pre" ).getText(), containsString( url ) );

        // Lookup
        click( findFirst( "#list-output div.panel-body div.pull-right a" ) );
        await().until( "#api-output pre" ).hasSize( 5 );
        assertThat( findFirst( "#api-output pre" ).getText(), containsString( hash ) );

        // Redirect
        String mainWindow = getDriver().getWindowHandle();
        click( findFirst( "#list-output div.panel-heading a" ) );
        await().until( new Predicate<Object>()
        {
            @Override
            public boolean apply( Object input )
            {
                return getDriver().getWindowHandles().size() == 2;
            }
        } );
        String popupWindow = null;
        for( String window : getDriver().getWindowHandles() )
        {
            if( !mainWindow.equals( window ) )
            {
                popupWindow = window;
            }
        }
        assertThat( popupWindow, notNullValue() );
        getDriver().switchTo().window( popupWindow );
        await().untilPage();
        assertThat( pageSource(), containsString( "Bootstrap" ) );
    }
}
