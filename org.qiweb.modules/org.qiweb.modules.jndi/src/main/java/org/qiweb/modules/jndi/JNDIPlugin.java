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

import javax.naming.NamingException;
import org.qiweb.api.Application;
import org.qiweb.api.Config;
import org.qiweb.api.Plugin;
import org.qiweb.api.exceptions.ActivationException;

/**
 * JNDI Plugin.
 *
 * Work in embedded mode by default, providing an in-memory JNDI context dedicated to the Application backed by
 * <a href="http://tyrex.sourceforge.net/naming.html">Tyrex</a>.
 * The JNDI context is cleared on passivation.
 * <p>
 * If the {@literal jndi.embedded} configuration property is set to {@literal false} then the embedded mode is disabled.
 * <p>
 * If you are running your Application in a container that provide JNDI you want to disable the embedded mode and filter
 * the {@literal tyrex} jar out of the classpath.
 */
public class JNDIPlugin
    implements Plugin<JNDI>
{
    private JNDI jndi;

    @Override
    public void onActivate( Application application )
        throws ActivationException
    {
        Config config = application.config();
        try
        {
            jndi = new JNDI(
                config.bool( "jndi.embedded" )
            );
        }
        catch( NamingException ex )
        {
            throw new ActivationException( "JNDI Plugin activation failed", ex );
        }
    }

    @Override
    public void onPassivate( Application application )
    {
        if( jndi != null )
        {
            try
            {
                jndi.passivate();
            }
            catch( NamingException ex )
            {
                throw new ActivationException( "JNDI Plugin passivation failed", ex );
            }
            finally
            {
                jndi = null;
            }
        }
    }

    @Override
    public Class<JNDI> apiType()
    {
        return JNDI.class;
    }

    @Override
    public JNDI api()
    {
        return jndi;
    }
}
