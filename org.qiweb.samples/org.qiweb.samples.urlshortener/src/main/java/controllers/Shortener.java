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
package controllers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Shortener
{

    public static final Shortener INSTANCE = new Shortener();
    private static final int LENGTH = 4;
    private static final char[] CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private final Map<String, String> shortened = new HashMap<>();
    private final Random rng = new Random();

    public Map<String, String> list()
    {
        return Collections.unmodifiableMap( shortened );
    }

    public String shorten( String longUrl )
    {
        String hash = generateNewHash();
        shortened.put( hash, longUrl );
        return hash;
    }

    public String expand( String hash )
    {
        return shortened.get( hash );
    }

    public String lookup( String longUrl )
    {
        for( Map.Entry<String, String> entry : shortened.entrySet() )
        {
            if( entry.getValue().equals( longUrl ) )
            {
                return entry.getKey();
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

    private Shortener()
    {
    }
}
