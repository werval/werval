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

import java.math.BigDecimal;
import java.nio.charset.Charset;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;
import org.qiweb.modules.metrics.Metrics;
import org.qiweb.modules.smtp.internal.MultiPartEmailMetrics;
import org.qiweb.modules.smtp.internal.SimpleEmailMetrics;

/**
 * SMTP.
 */
public class Smtp
{
    private final String host;
    private final int port;
    private final Transport transport;
    private final boolean validateCertificate;
    private final long connectionTimeout;
    private final long ioTimeout;
    private final String username;
    private final String password;
    private final String bounce;
    private final Charset charset;
    private final Metrics metrics;

    /* package */ Smtp(
        String host, int port,
        Transport transport, boolean validateCertificate,
        long connectionTimeout, long ioTimeout,
        String username, String password,
        String bounce,
        Charset charset,
        Metrics metrics
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
        this.bounce = bounce;
        this.charset = charset;
        this.metrics = metrics;
    }

    public SimpleEmail newSimpleEmail()
    {
        return configured( metrics == null ? new SimpleEmail() : new SimpleEmailMetrics( metrics ) );
    }

    public MultiPartEmail newMultiPartEmail()
    {
        return configured( metrics == null ? new MultiPartEmail() : new MultiPartEmailMetrics( metrics ) );
    }

    public EmailAttachment newAttachment()
    {
        return new EmailAttachment();
    }

    private <T extends Email> T configured( T email )
    {
        email.setHostName( host );
        email.setSmtpPort( port );
        email.setSSLOnConnect( transport == Transport.SSL );
        email.setStartTLSEnabled( transport == Transport.TLS );
        email.setStartTLSRequired( transport == Transport.TLS );
        email.setSSLCheckServerIdentity( validateCertificate );
        email.setSocketConnectionTimeout( new BigDecimal( connectionTimeout ).intValueExact() );
        email.setSocketTimeout( new BigDecimal( ioTimeout ).intValueExact() );
        if( username != null )
        {
            email.setAuthentication( username, password );
        }
        if( bounce != null )
        {
            email.setBounceAddress( bounce );
        }
        email.setCharset( charset.name() );
        return email;
    }
}
