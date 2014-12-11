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
package io.werval.runtime.mime;

import io.werval.api.mime.MediaRange;
import io.werval.util.Couple;
import io.werval.util.Strings;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import static io.werval.api.mime.MimeTypes.WILDCARD_MIMETYPE;
import static io.werval.util.Strings.EMPTY;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;

/**
 * Media Range Instance.
 */
public final class MediaRangeInstance
    implements MediaRange
{
    private static final MediaRange WILDCARD = new MediaRangeInstance( "*", "*", 1D, emptyList() );
    private static final Comparator<MediaRange> COMPARATOR = (range1, range2) ->
    {
        if( range1.qValue() == range2.qValue() )
        {
            return Integer.compare( range2.acceptExtensions().size(), range1.acceptExtensions().size() );
        }
        return Double.compare( range2.qValue(), range1.qValue() );
    };

    public static List<MediaRange> parseList( String pattern )
    {
        if( Strings.isEmpty( pattern ) )
        {
            return singletonList( WILDCARD );
        }
        List<MediaRange> result = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer( pattern, "," );
        while( tokenizer.hasMoreTokens() )
        {
            result.add( parseSingle( tokenizer.nextToken().trim() ) );
        }
        sort( result, COMPARATOR );
        return unmodifiableList( result );
    }

    public static MediaRange parseSingle( String pattern )
    {
        if( Strings.isEmpty( pattern ) )
        {
            return WILDCARD;
        }
        final String type;
        final String subtype;
        final StringTokenizer tokenizer;
        if( pattern.contains( "/" ) )
        {
            StringTokenizer tokens = new StringTokenizer( pattern, "/" );
            type = tokens.nextToken().trim();
            String subtypeAndParams = tokens.nextToken();
            tokenizer = new StringTokenizer( subtypeAndParams, ";" );
            subtype = tokenizer.nextToken().trim();
        }
        else
        {
            if( !pattern.startsWith( "*" ) )
            {
                throw new IllegalArgumentException( "Invalid MediaRange pattern: " + pattern );
            }
            type = "*";
            subtype = "*";
            tokenizer = new StringTokenizer( pattern, ";" );
            tokenizer.nextToken();
        }
        double qValue = 1D;
        final List<Couple<String, String>> acceptExtensions = new ArrayList<>();
        while( tokenizer.hasMoreTokens() )
        {
            String paramToken = tokenizer.nextToken();
            if( paramToken.contains( "=" ) )
            {
                StringTokenizer paramTokenizer = new StringTokenizer( paramToken, "=" );
                String key = paramTokenizer.nextToken().trim();
                if( "q".equalsIgnoreCase( key ) )
                {
                    qValue = Double.valueOf( paramTokenizer.nextToken().trim() );
                }
                else
                {
                    acceptExtensions.add( Couple.of( key, paramTokenizer.nextToken().trim() ) );
                }
            }
            else
            {
                acceptExtensions.add( Couple.of( paramToken.trim(), EMPTY ) );
            }
        }
        return new MediaRangeInstance( type, subtype, qValue, acceptExtensions );
    }

    public static boolean accepts( List<MediaRange> ranges, String mimeType )
    {
        if( ranges.isEmpty() )
        {
            return true;
        }
        return ranges.stream().anyMatch( range -> range.accepts( mimeType ) );
    }

    public static String preferred( List<MediaRange> ranges, String... mimeTypes )
    {
        if( ranges.isEmpty() && ( mimeTypes == null || mimeTypes.length == 0 ) )
        {
            // No Accept header, no candidate mimetype, return */*
            return WILDCARD_MIMETYPE;
        }
        if( ranges.isEmpty() )
        {
            // No Accept header, return first candidate mimetype
            return mimeTypes[0];
        }
        if( mimeTypes == null || mimeTypes.length == 0 )
        {
            // No candidate mimetype, return preferred from Accept header
            return ranges.get( 0 ).mimetype();
        }
        // Find best candidate mimetype among Accept header
        int score = Integer.MAX_VALUE;
        String preferred = null;
        for( String mimeType : mimeTypes )
        {
            for( int idx = 0; idx < ranges.size(); idx++ )
            {
                if( ranges.get( idx ).accepts( mimeType ) )
                {
                    if( idx < score )
                    {
                        score = idx;
                        preferred = ranges.get( idx ).mimetype();
                        break;
                    }
                }
            }
        }
        // If none found, return preferred from Accept header
        return preferred == null ? ranges.get( 0 ).mimetype() : preferred;
    }

    private final String type;
    private final String subtype;
    private final double qValue;
    private final List<Couple<String, String>> acceptExtensions;

    private MediaRangeInstance(
        String type, String subtype,
        double qValue, List<Couple<String, String>> acceptExtensions
    )
    {
        this.type = type;
        this.subtype = subtype;
        this.qValue = qValue;
        this.acceptExtensions = acceptExtensions;
    }

    @Override
    public String type()
    {
        return type;
    }

    @Override
    public String subtype()
    {
        return subtype;
    }

    @Override
    public String mimetype()
    {
        return type + "/" + subtype;
    }

    @Override
    public double qValue()
    {
        return qValue;
    }

    @Override
    public List<Couple<String, String>> acceptExtensions()
    {
        return unmodifiableList( acceptExtensions );
    }

    @Override
    public boolean accepts( String mimeType )
    {
        return ( type + "/" + subtype ).equalsIgnoreCase( mimeType )
               || ( "*".equals( subtype ) && type.equalsIgnoreCase( mimeType.substring( mimeType.indexOf( "/" ) ) ) )
               || ( "*".equals( type ) && "*".equals( subtype ) );
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( type ).append( "/" ).append( subtype );
        if( qValue != 1 )
        {
            sb.append( ";q=" ).append( qValue );
        }
        for( Couple<String, String> acceptExt : acceptExtensions )
        {
            sb.append( ";" ).append( acceptExt.left() );
            if( acceptExt.hasRight() )
            {
                sb.append( "=" ).append( acceptExt.right() );
            }
        }
        return sb.toString();
    }
}
