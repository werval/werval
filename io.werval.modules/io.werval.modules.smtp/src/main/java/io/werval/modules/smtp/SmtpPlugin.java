/*
 * Copyright (c) 2014-2015 the original author or authors
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
package io.werval.modules.smtp;

import io.werval.api.Application;
import io.werval.api.Config;
import io.werval.api.Plugin;
import io.werval.api.exceptions.ActivationException;
import io.werval.modules.metrics.Metrics;
import io.werval.modules.smtp.internal.SmtpHealthCheck;
import io.werval.modules.smtp.internal.Transport;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static java.util.Collections.EMPTY_LIST;

/**
 * SMTP Plugin.
 */
public class SmtpPlugin
    implements Plugin<Smtp>
{
    private Smtp smtp;

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

    @Override
    public List<Class<?>> dependencies( Config config )
    {
        if( config.bool( "smtp.metrics" ) )
        {
            return Arrays.asList( Metrics.class );
        }
        return EMPTY_LIST;
    }

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
        String host = config.string( "smtp.host" );
        boolean validateCert = config.bool( "smtp.validate_certificate" );
        long connTimeoutMillis = config.milliseconds( "smtp.connection_timeout" );
        long ioTimeoutMillis = config.milliseconds( "smtp.io_timeout" );
        String username = config.has( "smtp.username" ) ? config.string( "smtp.username" ) : null;
        String password = config.has( "smtp.password" ) ? config.string( "smtp.password" ) : null;
        String bounce = config.has( "smtp.bounce" ) ? config.string( "smtp.bounce" ) : null;
        boolean metrics = config.bool( "smtp.metrics" );
        smtp = new Smtp(
            host, port,
            transport, validateCert,
            connTimeoutMillis, ioTimeoutMillis,
            username, password,
            bounce,
            application.defaultCharset(),
            metrics ? application.plugin( Metrics.class ) : null
        );
        if( metrics )
        {
            application.plugin( Metrics.class ).healthChecks().register(
                "smtp",
                new SmtpHealthCheck(
                    host, port,
                    transport, validateCert,
                    connTimeoutMillis, ioTimeoutMillis,
                    username, password
                )
            );
        }
    }

    @Override
    public void onPassivate( Application application )
    {
        smtp = null;
    }
}
