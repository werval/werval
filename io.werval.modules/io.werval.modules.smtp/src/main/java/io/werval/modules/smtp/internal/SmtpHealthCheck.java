/*
 * Copyright (c) 2015 the original author or authors
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
package io.werval.modules.smtp.internal;

import com.codahale.metrics.health.HealthCheck;
import io.werval.util.Strings;
import java.util.Properties;
import javax.mail.Session;

/**
 * Smtp HealthCheck.
 */
public class SmtpHealthCheck
    extends HealthCheck
{
    private final String host;
    private final int port;
    private final Transport transport;
    private final boolean validateCertificate;
    private final long connectionTimeout;
    private final long ioTimeout;
    private final String username;
    private final String password;

    public SmtpHealthCheck(
        String host, int port,
        Transport transport, boolean validateCertificate,
        long connectionTimeout, long ioTimeout,
        String username, String password
    )
    {
        this.host = host;
        this.port = port;
        this.transport = transport;
        this.validateCertificate = validateCertificate;
        this.connectionTimeout = connectionTimeout;
        this.ioTimeout = ioTimeout;
        this.username = username;
        this.password = password;
    }

    @Override
    protected Result check()
        throws Exception
    {
        Properties props = new Properties();
        switch( transport )
        {
            case CLEAR:
                break;
            case SSL:
                break;
            case TLS:
                props.put( "mail.smtp.starttls.enable", "true" );
                break;
            default:
                // Should not happen
                throw new InternalError( "Invalid Transport" );
        }
        if( !Strings.isEmpty( username ) )
        {
            props.put( "mail.smtp.auth", "true" );
        }
        props.put( "mail.smtp.ssl.checkserveridentity", validateCertificate );
        props.put( "mail.smtp.connectiontimeout", Long.valueOf( connectionTimeout ).intValue() );
        props.put( "mail.smtp.timeout", Long.valueOf( ioTimeout ).intValue() );
        props.put( "mail.smtp.writetimeout", Long.valueOf( ioTimeout ).intValue() );
        Session session = Session.getInstance( props );
        javax.mail.Transport mailTransport = session.getTransport( "smtp" );
        try
        {
            mailTransport.connect( host, port, username, password );
        }
        finally
        {
            mailTransport.close();
        }
        return HealthCheck.Result.healthy();
    }
}
