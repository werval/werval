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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * URL Shortener Service.
 * <p><code>Singleton</code></p>
 */
public final class ShortenerService
{

    public static final ShortenerService INSTANCE = new ShortenerService();

    public static final class Link
    {

        public String hash;
        public String long_url;

        public static Link newInstance( String hash, String longUrl )
        {
            Link link = new Link();
            link.hash = hash;
            link.long_url = longUrl;
            return link;
        }
    }
    private static final int LENGTH = 4;
    private static final char[] CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private final Map<String, Link> shortened = new HashMap<>();
    private final Random rng = new Random();

    public Collection<Link> list()
    {
        return Collections.unmodifiableCollection( shortened.values() );
    }

    public String shorten( String longUrl )
    {
        String hash = generateNewHash();
        shortened.put( hash, Link.newInstance( hash, longUrl ) );
        return hash;
    }

    public String expand( String hash )
    {
        Link link = shortened.get( hash );
        return link == null ? null : link.long_url;
    }

    public String lookup( String longUrl )
    {
        for( Map.Entry<String, Link> entry : shortened.entrySet() )
        {
            Link link = entry.getValue();
            if( link.long_url.equals( longUrl ) )
            {
                return link.hash;
            }
        }
        return null;
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

    private ShortenerService()
    {
    }
}
