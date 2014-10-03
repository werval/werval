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
package org.qiweb.runtime.i18n;

import java.util.Locale;
import java.util.Objects;
import org.qiweb.api.i18n.Lang;

import static org.qiweb.api.util.IllegalArguments.ensureNotNull;
import static org.qiweb.api.util.Strings.EMPTY;

/**
 * Lang Instance.
 */
public class LangInstance
    implements Lang
{
    private final String language;
    private final String country;
    private final String code;

    public LangInstance( String language )
    {
        this( language, EMPTY );
    }

    public LangInstance( String language, String country )
    {
        ensureNotNull( "Language", language );
        this.language = language;
        this.country = country == null ? EMPTY : country;
        this.code = this.country.isEmpty() ? this.language : this.language + "-" + this.country;
    }

    @Override
    public String code()
    {
        return code;
    }

    @Override
    public String language()
    {
        return language;
    }

    @Override
    public String country()
    {
        return country;
    }

    @Override
    public Locale toLocale()
    {
        if( country.isEmpty() )
        {
            return new Locale( language );
        }
        return new Locale( language, country );
    }

    @Override
    public boolean satisfiedBy( Lang other )
    {
        if( !language.equalsIgnoreCase( other.language() ) )
        {
            return false;
        }
        if( other.country().isEmpty() )
        {
            return true;
        }
        return country.equalsIgnoreCase( other.country() );
    }

    @Override
    public String toString()
    {
        return code;
    }

    @Override
    public int hashCode()
    {
        return 185 + Objects.hashCode( code );
    }

    @Override
    public boolean equals( Object obj )
    {
        if( obj == null )
        {
            return false;
        }
        if( getClass() != obj.getClass() )
        {
            return false;
        }
        return Objects.equals( code, ( (LangInstance) obj ).code );
    }
}
