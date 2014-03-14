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
package org.qiweb.modules.jndi;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * JNDI.
 */
public class JNDI
{
    private final InitialContext initialContext;
    private final boolean embedded;
    private String previousUrl;

    public JNDI( boolean embedded )
        throws NamingException
    {
        this.embedded = embedded;
        if( embedded )
        {
            this.previousUrl = System.setProperty( Context.PROVIDER_URL, "/qiweb-embedded-jndi" );
        }
        this.initialContext = new InitialContext();
    }

    public InitialContext initialContext()
    {
        return initialContext;
    }

    /* package */ void passivate()
        throws NamingException
    {
        if( embedded )
        {
            // Clear embedded JNDI
            NamingEnumeration<NameClassPair> names = initialContext.list( "" );
            while( names.hasMore() )
            {
                initialContext.unbind( names.next().getName() );
            }
            // Set java.naming.provider.url System property back
            if( previousUrl == null )
            {
                System.clearProperty( Context.PROVIDER_URL );
            }
            else
            {
                System.setProperty( Context.PROVIDER_URL, previousUrl );
            }
        }
        initialContext.close();
    }
}
