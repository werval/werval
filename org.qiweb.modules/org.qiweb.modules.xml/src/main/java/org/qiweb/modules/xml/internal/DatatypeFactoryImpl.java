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
package org.qiweb.modules.xml.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * DatatypeFactory implementation for XMLPlugin.
 * <p>
 * Factory that creates new <code>javax.xml.datatype</code> <code>Object</code>s that map XML to/from Java
 * <code>Object</code>s.
 *
 * @see DatatypeFactory
 */
public final class DatatypeFactoryImpl
    extends DatatypeFactory
{
    // Xerces
    private final DatatypeFactory delegate = new org.apache.xerces.jaxp.datatype.DatatypeFactoryImpl();

    public DatatypeFactoryImpl()
    {
        super();
    }

    @Override
    public Duration newDuration( String lexicalRepresentation )
    {
        return delegate.newDuration( lexicalRepresentation );
    }

    @Override
    public Duration newDuration( long durationInMilliSeconds )
    {
        return delegate.newDuration( durationInMilliSeconds );
    }

    @Override
    public Duration newDuration(
        boolean isPositive,
        BigInteger years, BigInteger months, BigInteger days,
        BigInteger hours, BigInteger minutes, BigDecimal seconds
    )
    {
        return delegate.newDuration( isPositive, years, months, days, hours, minutes, seconds );
    }

    @Override
    public Duration newDuration(
        boolean isPositive,
        int years, int months, int days,
        int hours, int minutes, int seconds
    )
    {
        return delegate.newDuration( isPositive, years, months, days, hours, minutes, seconds );
    }

    @Override
    public Duration newDurationDayTime( String lexicalRepresentation )
    {
        return delegate.newDurationDayTime( lexicalRepresentation );
    }

    @Override
    public Duration newDurationDayTime( long durationInMilliseconds )
    {
        return delegate.newDurationDayTime( durationInMilliseconds );
    }

    @Override
    public Duration newDurationDayTime(
        boolean isPositive,
        BigInteger day,
        BigInteger hour, BigInteger minute, BigInteger second
    )
    {
        return delegate.newDurationDayTime( isPositive, day, hour, minute, second );
    }

    @Override
    public Duration newDurationDayTime(
        boolean isPositive,
        int day,
        int hour, int minute, int second
    )
    {
        return delegate.newDurationDayTime( isPositive, day, hour, minute, second );
    }

    @Override
    public Duration newDurationYearMonth( String lexicalRepresentation )
    {
        return delegate.newDurationYearMonth( lexicalRepresentation );
    }

    @Override
    public Duration newDurationYearMonth( long durationInMilliseconds )
    {
        return delegate.newDurationYearMonth( durationInMilliseconds );
    }

    @Override
    public Duration newDurationYearMonth( boolean isPositive, BigInteger year, BigInteger month )
    {
        return delegate.newDurationYearMonth( isPositive, year, month );
    }

    @Override
    public Duration newDurationYearMonth( boolean isPositive, int year, int month )
    {
        return delegate.newDurationYearMonth( isPositive, year, month );
    }

    @Override
    public XMLGregorianCalendar newXMLGregorianCalendar()
    {
        return delegate.newXMLGregorianCalendar();
    }

    @Override
    public XMLGregorianCalendar newXMLGregorianCalendar( String lexicalRepresentation )
    {
        return delegate.newXMLGregorianCalendar( lexicalRepresentation );
    }

    @Override
    public XMLGregorianCalendar newXMLGregorianCalendar( GregorianCalendar cal )
    {
        return delegate.newXMLGregorianCalendar( cal );
    }

    @Override
    public XMLGregorianCalendar newXMLGregorianCalendar(
        BigInteger year, int month, int day,
        int hour, int minute, int second, BigDecimal fractionalSecond,
        int timezone
    )
    {
        return delegate.newXMLGregorianCalendar( year, month, day, hour, minute, second, fractionalSecond, timezone );
    }

    @Override
    public XMLGregorianCalendar newXMLGregorianCalendar(
        int year, int month, int day,
        int hour, int minute, int second, int millisecond,
        int timezone
    )
    {
        return delegate.newXMLGregorianCalendar( year, month, day, hour, minute, second, millisecond, timezone );
    }

    @Override
    public XMLGregorianCalendar newXMLGregorianCalendarDate( int year, int month, int day, int timezone )
    {
        return delegate.newXMLGregorianCalendarDate( year, month, day, timezone );
    }

    @Override
    public XMLGregorianCalendar newXMLGregorianCalendarTime( int hours, int minutes, int seconds, int timezone )
    {
        return delegate.newXMLGregorianCalendarTime( hours, minutes, seconds, timezone );
    }

    @Override
    public XMLGregorianCalendar newXMLGregorianCalendarTime(
        int hours, int minutes, int seconds, BigDecimal fractionalSecond,
        int timezone
    )
    {
        return delegate.newXMLGregorianCalendarTime( hours, minutes, seconds, fractionalSecond, timezone );
    }

    @Override
    public XMLGregorianCalendar newXMLGregorianCalendarTime(
        int hours, int minutes, int seconds, int milliseconds,
        int timezone
    )
    {
        return delegate.newXMLGregorianCalendarTime( hours, minutes, seconds, milliseconds, timezone );
    }
}
