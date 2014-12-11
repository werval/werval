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

import com.codahale.metrics.MetricRegistry;
import io.werval.test.QiWebRule;
import java.util.List;
import javax.mail.MessagingException;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.modules.metrics.Metrics;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Assert that SmtpPlugin works.
 */
public class SmtpPluginTest
{
    @ClassRule
    public static final QiWebRule QIWEB = new QiWebRule();
    private Wiser wiser;

    @Before
    public void setUpWiser()
    {
        wiser = new Wiser();
        wiser.setPort( 23025 );
        wiser.start();
    }

    @After
    public void tearDownWiser()
    {
        if( wiser != null )
        {
            wiser.stop();
        }
    }

    @Test
    public void testSimpleEmail()
        throws EmailException, MessagingException
    {
        Smtp smtp = QIWEB.application().plugin( Smtp.class );
        assertThat( smtp, notNullValue() );

        SimpleEmail email = smtp.newSimpleEmail();
        email.setFrom( "from@qiweb.org" );
        email.addTo( "to@qiweb.org" );
        email.setSubject( "Simple Email" );
        email.send();

        List<WiserMessage> messages = wiser.getMessages();
        assertThat( messages.size(), is( 1 ) );
        WiserMessage message = messages.get( 0 );
        assertThat( message.getMimeMessage().getHeader( "From" )[0], equalTo( "from@qiweb.org" ) );
        assertThat( message.getMimeMessage().getHeader( "To" )[0], equalTo( "to@qiweb.org" ) );
        assertThat( message.getMimeMessage().getHeader( "Subject" )[0], equalTo( "Simple Email" ) );

        MetricRegistry metrics = QIWEB.application().plugin( Metrics.class ).metrics();
        assertThat( metrics.meter( "org.qiweb.modules.smtp.sent" ).getCount(), is( 1L ) );
        assertThat( metrics.meter( "org.qiweb.modules.smtp.errors" ).getCount(), is( 0L ) );
    }
}
