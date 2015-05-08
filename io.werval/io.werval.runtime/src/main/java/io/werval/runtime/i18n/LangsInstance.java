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
package io.werval.runtime.i18n;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.werval.api.i18n.Lang;
import io.werval.api.i18n.Langs;

import static java.util.stream.Collectors.toList;

/**
 * Langs Instance.
 */
public class LangsInstance
    implements Langs
{
    private static final Pattern COUNTRY_PATTERN = Pattern.compile( "([a-zA-Z]{2})-([a-zA-Z]{2}|[0-9]{3})" );
    private static final Pattern LANGUAGE_PATTERN = Pattern.compile( "([a-zA-Z]{2})" );
    private final List<String> availableLangs;

    public LangsInstance( List<String> availableLangs )
    {
        this.availableLangs = availableLangs;
    }

    @Override
    public Lang defaultLang()
    {
        Locale defloc = Locale.getDefault();
        return new LangInstance( defloc.getLanguage(), defloc.getCountry() );
    }

    @Override
    public List<Lang> availables()
    {
        return availableLangs.stream().map( lang -> fromCode( lang ) ).collect( toList() );
    }

    @Override
    public Lang preferred( List<Lang> langs )
    {
        List<Lang> availables = availables();
        if( availables.size() > 0 )
        {
            for( Lang lang : langs )
            {
                for( Lang avail : availables )
                {
                    if( avail.satisfiedBy( lang ) )
                    {
                        return avail;
                    }
                }
            }
            return availables.get( 0 );
        }
        return defaultLang();
    }

    @Override
    public Lang fromCode( String code )
    {
        Matcher country = COUNTRY_PATTERN.matcher( code );
        if( country.matches() )
        {
            return new LangInstance( country.group( 1 ), country.group( 2 ) );
        }
        Matcher lang = LANGUAGE_PATTERN.matcher( code );
        if( lang.matches() )
        {
            return new LangInstance( lang.group( 1 ) );
        }
        throw new IllegalArgumentException( String.format( "Invalid language: %s", code ) );
    }
}
