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
package org.qiweb.modules.smtp;

import java.util.Locale;
import org.qiweb.api.Application;
import org.qiweb.api.Config;
import org.qiweb.api.PluginAdapter;
import org.qiweb.api.exceptions.ActivationException;

/**
 * SMTP Plugin.
 */
public class SmtpPlugin
    extends PluginAdapter<Smtp>
{
    private Smtp smtp;

    @Override
    public void onActivate( Application application )
        throws ActivationException
    {
        Config config = application.config();
        Transport transport = Transport.valueOf( config.string( "smtp.transport" ).toUpperCase( Locale.US ) );
        int port;
        if( config.has( "smtp.port" ) )
        {
            port = config.intNumber( "smtp.port" );
        }
        else
        {
            switch( transport )
            {
                case CLEAR:
                    port = 25;
                    break;
                case SSL:
                    port = 465;
                    break;
                case TLS:
                    port = 25;
                    break;
                default:
                    // Should not happen
                    throw new InternalError( "Invalid Transport" );
            }
        }
        smtp = new Smtp(
            config.string( "smtp.host" ),
            port,
            transport,
            config.bool( "smtp.validate_certificate" ),
            config.milliseconds( "smtp.connection_timeout" ),
            config.milliseconds( "smtp.io_timeout" ),
            config.has( "smtp.username" ) ? config.string( "smtp.username" ) : null,
            config.has( "smtp.password" ) ? config.string( "smtp.password" ) : null,
            config.has( "smtp.bounce" ) ? config.string( "smtp.bounce" ) : null,
            application.defaultCharset()
        );
    }

    @Override
    public void onPassivate( Application application )
    {
        smtp = null;
    }

    @Override
    public Class<Smtp> apiType()
    {
        return Smtp.class;
    }

    @Override
    public Smtp api()
    {
        return smtp;
    }
}
