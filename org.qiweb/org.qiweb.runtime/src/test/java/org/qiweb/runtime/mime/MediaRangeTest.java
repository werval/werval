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
package org.qiweb.runtime.mime;

import io.werval.api.mime.MediaRange;
import io.werval.util.Couple;
import java.util.List;
import org.junit.Test;

import static io.werval.api.mime.MimeTypes.APPLICATION_ATOM_XML;
import static io.werval.api.mime.MimeTypes.APPLICATION_JSON;
import static io.werval.api.mime.MimeTypes.IMAGE_JPEG;
import static io.werval.api.mime.MimeTypes.TEXT_HTML;
import static io.werval.api.mime.MimeTypes.TEXT_PLAIN;
import static io.werval.api.mime.MimeTypes.TEXT_X_C;
import static io.werval.api.mime.MimeTypes.VIDEO_MP4;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * MediaRange Test.
 */
public class MediaRangeTest
{
    @Test
    public void parseSingleEmpty()
    {
        MediaRange range = MediaRangeInstance.parseSingle( null );
        assertThat( range.type(), equalTo( "*" ) );
        assertThat( range.subtype(), equalTo( "*" ) );
        assertThat( range.qValue(), is( 1D ) );
        assertTrue( range.acceptExtensions().isEmpty() );
        assertThat( range.toString(), equalTo( "*/*" ) );
        assertTrue( range.accepts( APPLICATION_ATOM_XML ) );
        assertTrue( range.accepts( TEXT_PLAIN ) );
    }

    @Test
    public void parseSingleBasic()
    {
        MediaRange range = MediaRangeInstance.parseSingle( APPLICATION_ATOM_XML );
        assertThat( range.type(), equalTo( "application" ) );
        assertThat( range.subtype(), equalTo( "atom+xml" ) );
        assertThat( range.qValue(), is( 1D ) );
        assertTrue( range.acceptExtensions().isEmpty() );
        assertThat( range.toString(), equalTo( APPLICATION_ATOM_XML ) );
        assertTrue( range.accepts( APPLICATION_ATOM_XML ) );
        assertFalse( range.accepts( APPLICATION_JSON ) );
    }

    @Test
    public void parseSingleQValue()
    {
        MediaRange range = MediaRangeInstance.parseSingle( "application/json;q=0.2" );
        assertThat( range.type(), equalTo( "application" ) );
        assertThat( range.subtype(), equalTo( "json" ) );
        assertThat( range.qValue(), is( 0.2D ) );
        assertTrue( range.acceptExtensions().isEmpty() );
        assertThat( range.toString(), equalTo( "application/json;q=0.2" ) );
        assertTrue( range.accepts( APPLICATION_JSON ) );
        assertFalse( range.accepts( APPLICATION_ATOM_XML ) );
    }

    @Test
    public void parseSingleComplex()
    {
        MediaRange range = MediaRangeInstance.parseSingle( "application/json;q=0.2;foo=bar;baz=bazar" );
        assertThat( range.type(), equalTo( "application" ) );
        assertThat( range.subtype(), equalTo( "json" ) );
        assertThat( range.qValue(), is( 0.2D ) );
        assertThat( range.acceptExtensions().size(), is( 2 ) );
        assertThat( range.acceptExtensions().get( 0 ), equalTo( Couple.of( "foo", "bar" ) ) );
        assertThat( range.acceptExtensions().get( 1 ), equalTo( Couple.of( "baz", "bazar" ) ) );
        assertThat( range.toString(), equalTo( "application/json;q=0.2;foo=bar;baz=bazar" ) );
        assertTrue( range.accepts( APPLICATION_JSON ) );
        assertFalse( range.accepts( APPLICATION_ATOM_XML ) );
    }

    @Test
    public void parseListEmpty()
    {
        List<MediaRange> ranges = MediaRangeInstance.parseList( null );
        assertThat( ranges.size(), is( 1 ) );
        MediaRange range = ranges.get( 0 );
        assertThat( range.type(), equalTo( "*" ) );
        assertThat( range.subtype(), equalTo( "*" ) );
        assertThat( range.qValue(), is( 1D ) );
        assertTrue( range.acceptExtensions().isEmpty() );
        assertTrue( range.accepts( APPLICATION_JSON ) );
        assertTrue( range.accepts( TEXT_PLAIN ) );
    }

    @Test
    public void parseListBasic()
    {
        List<MediaRange> ranges = MediaRangeInstance.parseList( APPLICATION_JSON + "," + TEXT_PLAIN );
        assertThat( ranges.size(), is( 2 ) );
        assertThat( ranges.get( 0 ).toString(), equalTo( APPLICATION_JSON ) );
        assertThat( ranges.get( 1 ).toString(), equalTo( TEXT_PLAIN ) );
    }

    @Test
    public void parseListQValue()
    {
        List<MediaRange> ranges = MediaRangeInstance.parseList(
            "text/plain; q=0.5, text/html,text/x-dvi; q=0.8, text/x-c"
        );
        assertThat( ranges.size(), is( 4 ) );
        assertThat( ranges.get( 0 ).toString(), equalTo( TEXT_HTML ) );
        assertThat( ranges.get( 1 ).toString(), equalTo( TEXT_X_C ) );
        assertThat( ranges.get( 2 ).toString(), equalTo( "text/x-dvi;q=0.8" ) );
        assertThat( ranges.get( 3 ).toString(), equalTo( "text/plain;q=0.5" ) );
    }

    @Test
    public void parseListComplex()
    {
        List<MediaRange> ranges = MediaRangeInstance.parseList(
            "text/*;q=0.3, text/html;q=0.7, text/html;level=1,text/html;level=2;q=0.4, */*;q=0.5"
        );
        assertThat( ranges.size(), is( 5 ) );
        assertThat( ranges.get( 0 ).toString(), equalTo( "text/html;level=1" ) );
        assertThat( ranges.get( 1 ).toString(), equalTo( "text/html;q=0.7" ) );
        assertThat( ranges.get( 2 ).toString(), equalTo( "*/*;q=0.5" ) );
        assertThat( ranges.get( 3 ).toString(), equalTo( "text/html;q=0.4;level=2" ) );
        assertThat( ranges.get( 4 ).toString(), equalTo( "text/*;q=0.3" ) );
        assertTrue( MediaRangeInstance.accepts( ranges, TEXT_HTML ) );
        assertTrue( MediaRangeInstance.accepts( ranges, TEXT_PLAIN ) );
        assertTrue( MediaRangeInstance.accepts( ranges, IMAGE_JPEG ) );
        assertTrue( MediaRangeInstance.accepts( ranges, VIDEO_MP4 ) );
    }
}
