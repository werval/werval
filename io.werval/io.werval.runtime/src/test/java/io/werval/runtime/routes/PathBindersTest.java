/*
 * Copyright (c) 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.werval.runtime.routes;

import com.acme.app.CustomParam;
import io.werval.api.routes.ParameterBinder;
import io.werval.api.routes.ParameterBinders;
import io.werval.test.WervalHttpRule;
import io.werval.util.Hashid;
import io.werval.util.Hashids;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import static io.werval.runtime.ConfigKeys.APP_SECRET;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PathBindersTest
{
    @ClassRule
    public static final WervalHttpRule WERVAL = new WervalHttpRule();
    private static final String NAME = "name";
    private static ParameterBinders binders;

    @BeforeClass
    public static void setupParameterBinders()
    {
        List<ParameterBinder<? extends Object>> list = Arrays.asList(
            new ParameterBindersInstance.String(),
            new ParameterBindersInstance.Boolean(),
            new ParameterBindersInstance.Short(),
            new ParameterBindersInstance.Integer(),
            new ParameterBindersInstance.Long(),
            new ParameterBindersInstance.Double(),
            new ParameterBindersInstance.Float(),
            new ParameterBindersInstance.BigInteger(),
            new ParameterBindersInstance.BigDecimal(),
            new ParameterBindersInstance.UUID(),
            new ParameterBindersInstance.URL(),
            new ParameterBindersInstance.Class(),
            new ParameterBindersInstance.Duration(),
            new ParameterBindersInstance.Period(),
            new ParameterBindersInstance.Year(),
            new ParameterBindersInstance.Month(),
            new ParameterBindersInstance.DayOfWeek(),
            new ParameterBindersInstance.YearMonth(),
            new ParameterBindersInstance.MonthDay(),
            new ParameterBindersInstance.LocalDate(),
            new ParameterBindersInstance.LocalTime(),
            new ParameterBindersInstance.LocalDateTime(),
            new ParameterBindersInstance.OffsetTime(),
            new ParameterBindersInstance.OffsetDateTime(),
            new ParameterBindersInstance.ZonedDateTime(),
            new ParameterBindersInstance.Hashid(),
            new CustomParam.ParameterBinder()
        );
        list.forEach( b -> b.onActivate( WERVAL.application() ) );
        binders = new ParameterBindersInstance( list );
    }

    @Test
    public void stringBinder()
        throws MalformedURLException
    {
        assertThat(
            binders.bind( String.class, NAME, "foo" ),
            equalTo( "foo" )
        );
        assertThat(
            binders.unbind( String.class, NAME, "foo" ),
            equalTo( "foo" )
        );
    }

    @Test
    public void booleanBinder()
    {
        assertThat(
            binders.bind( Boolean.class, NAME, "true" ),
            is( true )
        );
        assertThat(
            binders.unbind( Boolean.class, NAME, true ),
            equalTo( "true" )
        );
    }

    @Test
    public void shortBinder()
    {
        assertThat(
            binders.bind( Short.class, NAME, "23" ),
            equalTo( (short) 23 )
        );
        assertThat(
            binders.unbind( Short.class, NAME, (short) 42 ),
            equalTo( "42" )
        );
    }

    @Test
    public void intBinder()
    {
        assertThat(
            binders.bind( Integer.class, NAME, "23" ),
            equalTo( 23 )
        );
        assertThat(
            binders.unbind( Integer.class, NAME, 42 ),
            equalTo( "42" )
        );
    }

    @Test
    public void longBinder()
    {
        assertThat(
            binders.bind( Long.class, NAME, "23" ),
            equalTo( 23L )
        );
        assertThat(
            binders.unbind( Long.class, NAME, 42L ),
            equalTo( "42" )
        );
    }

    @Test
    public void doubleBinder()
    {
        assertThat(
            binders.bind( Double.class, NAME, "23.42" ),
            equalTo( 23.42D )
        );
        assertThat(
            binders.unbind( Double.class, NAME, 42.23D ),
            equalTo( "42.23" )
        );
    }

    @Test
    public void floatBinder()
    {
        assertThat(
            binders.bind( Float.class, NAME, "23.42" ),
            equalTo( 23.42F )
        );
        assertThat(
            binders.unbind( Float.class, NAME, 42.23F ),
            equalTo( "42.23" )
        );
    }

    @Test
    public void bigIntegerBinder()
    {
        assertThat(
            binders.bind( BigInteger.class, NAME, "23" ),
            equalTo( new BigInteger( "23" ) )
        );
        assertThat(
            binders.unbind( BigInteger.class, NAME, new BigInteger( "42" ) ),
            equalTo( "42" )
        );
    }

    @Test
    public void bigDecimalBinder()
    {
        assertThat(
            binders.bind( BigDecimal.class, NAME, "23.42" ),
            equalTo( new BigDecimal( "23.42" ) )
        );
        assertThat(
            binders.unbind( BigDecimal.class, NAME, new BigDecimal( "42.23" ) ),
            equalTo( "42.23" )
        );
    }

    @Test
    public void uuidBinder()
    {
        assertThat(
            binders.bind( UUID.class, NAME, "E461082E-B97B-4478-BFB5-DA6C79AFD3F8" ),
            equalTo( UUID.fromString( "E461082E-B97B-4478-BFB5-DA6C79AFD3F8" ) )
        );
        assertThat(
            binders.unbind( UUID.class, NAME, UUID.fromString( "E461082E-B97B-4478-BFB5-DA6C79AFD3F8" ) ),
            equalTo( "E461082E-B97B-4478-BFB5-DA6C79AFD3F8".toLowerCase( Locale.US ) )
        );
    }

    @Test
    public void urlBinder()
        throws MalformedURLException
    {
        assertThat(
            binders.bind( URL.class, NAME, "http://werval.io" ),
            equalTo( new URL( "http://werval.io" ) )
        );
        assertThat(
            binders.unbind( URL.class, NAME, new URL( "http://werval.io" ) ),
            equalTo( "http://werval.io" )
        );
    }

    @Test
    public void classBinder()
    {
        assertThat(
            binders.bind( Class.class, NAME, "java.util.Arrays" ),
            equalTo( Arrays.class )
        );
        assertThat(
            binders.unbind( Class.class, NAME, Arrays.class ),
            equalTo( "java.util.Arrays" )
        );
    }

    @Test
    public void durationBinder()
    {
        assertThat(
            binders.bind( Duration.class, NAME, "PT12H" ),
            equalTo( Duration.ofHours( 12 ) )
        );
        assertThat(
            binders.unbind( Duration.class, NAME, Duration.ofHours( 12 ) ),
            equalTo( "PT12H" )
        );
    }

    @Test
    public void periodBinder()
    {
        assertThat(
            binders.bind( Period.class, NAME, "P12D" ),
            equalTo( Period.ofDays( 12 ) )
        );
        assertThat(
            binders.unbind( Period.class, NAME, Period.ofDays( 12 ) ),
            equalTo( "P12D" )
        );
    }

    @Test
    public void yearBinder()
    {
        assertThat(
            binders.bind( Year.class, NAME, "1969" ),
            equalTo( Year.of( 1969 ) )
        );
        assertThat(
            binders.unbind( Year.class, NAME, Year.of( 1969 ) ),
            equalTo( "1969" )
        );
    }

    @Test
    public void monthBinder()
    {
        assertThat(
            binders.bind( Month.class, NAME, "1" ),
            equalTo( Month.JANUARY )
        );
        assertThat(
            binders.unbind( Month.class, NAME, Month.DECEMBER ),
            equalTo( "12" )
        );
    }

    @Test
    public void dayOfWeekBinder()
    {
        assertThat(
            binders.bind( DayOfWeek.class, NAME, "1" ),
            equalTo( DayOfWeek.MONDAY )
        );
        assertThat(
            binders.unbind( DayOfWeek.class, NAME, DayOfWeek.SUNDAY ),
            equalTo( "7" )
        );
    }

    @Test
    public void yearMonthBinder()
    {
        assertThat(
            binders.bind( YearMonth.class, NAME, "1969-06" ),
            equalTo( YearMonth.of( 1969, 6 ) )
        );
        assertThat(
            binders.unbind( YearMonth.class, NAME, YearMonth.of( 1969, 6 ) ),
            equalTo( "1969-06" )
        );
    }

    @Test
    public void monthDayBinder()
    {
        assertThat(
            binders.bind( MonthDay.class, NAME, "--01-01" ),
            equalTo( MonthDay.of( 1, 1 ) )
        );
        assertThat(
            binders.unbind( MonthDay.class, NAME, MonthDay.of( 12, 31 ) ),
            equalTo( "--12-31" )
        );
    }

    @Test
    public void localDateBinder()
    {
        assertThat(
            binders.bind( LocalDate.class, NAME, "2007-12-03" ),
            equalTo( LocalDate.of( 2007, 12, 3 ) )
        );
        assertThat(
            binders.unbind( LocalDate.class, NAME, LocalDate.of( 2007, 12, 3 ) ),
            equalTo( "2007-12-03" )
        );
    }

    @Test
    public void localTimeBinder()
    {
        assertThat(
            binders.bind( LocalTime.class, NAME, "23:42:23" ),
            equalTo( LocalTime.of( 23, 42, 23 ) )
        );
        assertThat(
            binders.unbind( LocalTime.class, NAME, LocalTime.of( 23, 42, 23 ) ),
            equalTo( "23:42:23" )
        );
    }

    @Test
    public void localDateTimeBinder()
    {
        assertThat(
            binders.bind( LocalDateTime.class, NAME, "2007-12-03T23:42:23" ),
            equalTo( LocalDateTime.of( 2007, 12, 3, 23, 42, 23 ) )
        );
        assertThat(
            binders.unbind( LocalDateTime.class, NAME, LocalDateTime.of( 2007, 12, 3, 23, 42, 23 ) ),
            equalTo( "2007-12-03T23:42:23" )
        );
    }

    @Test
    public void offsetTimeBinder()
    {
        assertThat(
            binders.bind( OffsetTime.class, NAME, "23:42:23+01:00" ),
            equalTo( OffsetTime.of( 23, 42, 23, 0, ZoneOffset.ofHours( 1 ) ) )
        );
        assertThat(
            binders.unbind( OffsetTime.class, NAME, OffsetTime.of( 23, 42, 23, 0, ZoneOffset.UTC ) ),
            equalTo( "23:42:23Z" )
        );
    }

    @Test
    public void offsetDateTimeBinder()
    {
        assertThat(
            binders.bind( OffsetDateTime.class, NAME, "2007-12-03T23:42:23Z" ),
            equalTo( OffsetDateTime.of( 2007, 12, 3, 23, 42, 23, 0, ZoneOffset.UTC ) )
        );
        assertThat(
            binders.unbind(
                OffsetDateTime.class,
                NAME,
                OffsetDateTime.of( 2007, 12, 3, 23, 42, 23, 0, ZoneOffset.ofHours( 2 ) )
            ),
            equalTo( "2007-12-03T23:42:23+02:00" )
        );
    }

    @Test
    public void zonedDateTimeBinder()
    {
        assertThat(
            binders.bind( ZonedDateTime.class, NAME, "2007-12-03T23:42:23Z" ),
            equalTo( ZonedDateTime.of( 2007, 12, 3, 23, 42, 23, 0, ZoneOffset.UTC ) )
        );
        assertThat(
            binders.unbind(
                ZonedDateTime.class,
                NAME,
                ZonedDateTime.of( 2007, 12, 3, 23, 42, 23, 0, ZoneOffset.ofHours( 2 ) )
            ),
            equalTo( "2007-12-03T23:42:23+02:00" )
        );
    }

    @Test
    public void hashidBinder()
    {
        Hashid hashid = new Hashids( WERVAL.application().config().string( APP_SECRET ) ).encode( 23L );
        assertThat(
            binders.bind( Hashid.class, NAME, hashid.toString() ),
            equalTo( hashid )
        );
        assertThat(
            binders.unbind( Hashid.class, NAME, hashid ),
            equalTo( hashid.toString() )
        );
    }

    @Test
    public void customBinder()
    {
        assertThat(
            binders.bind( CustomParam.class, NAME, "1234" ),
            equalTo( new CustomParam( "1234" ) )
        );
        assertThat(
            binders.unbind( CustomParam.class, NAME, new CustomParam( "1234" ) ),
            equalTo( "1234" )
        );
    }

    @Test
    public void polymorphism()
    {
        ParameterBinder<Integer> binder = new ParameterBindersInstance.Integer();
        assertThat( binder.accept( String.class ), is( false ) );
        assertThat( binder.accept( Number.class ), is( false ) );
        assertThat( binder.accept( Integer.class ), is( true ) );
    }
}
