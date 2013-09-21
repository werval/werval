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
package urlshortener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * URL Shortener Service.
 */
public final class ShortenerService
{

    private static final int LENGTH = 4;
    private static final char[] CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private final Map<String, Link> shortened = new HashMap<>();
    private final Random rng = new Random();

    public Collection<Link> list()
    {
        return Collections.unmodifiableCollection( shortened.values() );
    }

    public Link link( String hash )
    {
        return shortened.get( hash );
    }

    public Link shorten( String longUrl )
    {
        Link link = Link.newInstance( generateNewHash(), longUrl );
        shortened.put( link.hash, link );
        return link;
    }

    public Collection<Link> lookup( String longUrl )
    {
        List<Link> list = new ArrayList<>();
        for( Map.Entry<String, Link> entry : shortened.entrySet() )
        {
            Link link = entry.getValue();
            if( link.longUrl.equals( longUrl ) )
            {
                list.add( link );
            }
        }
        return list;
    }

    private String generateNewHash()
    {
        while( true )
        {
            StringBuilder sb = new StringBuilder();
            for( int idx = 0; idx < LENGTH; idx++ )
            {
                sb.append( CHARS[rng.nextInt( CHARS.length )] );
            }
            String hash = sb.toString();
            if( !shortened.containsKey( hash ) )
            {
                return hash;
            }
        }
    }
}
