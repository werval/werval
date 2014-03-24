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

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.qiweb.test.QiWebHttpRule;

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
    public static final QiWebHttpRule QIWEB = new QiWebHttpRule();
    private SimpleSmtpServer smtpServer;

    @Before
    public void setDumbsterUp()
    {
        smtpServer = SimpleSmtpServer.start( 23025 );
    }

    @After
    public void tearDumbsterDown()
    {
        if( smtpServer != null )
        {
            smtpServer.stop();
        }
    }

    @Test
    public void testSimpleEmail()
        throws EmailException
    {
        Smtp smtp = QIWEB.application().plugin( Smtp.class );
        assertThat( smtp, notNullValue() );

        SimpleEmail email = smtp.newSimpleEmail();
        email.setFrom( "from@qiweb.org" );
        email.addTo( "to@qiweb.org" );
        email.setSubject( "Simple Email" );
        email.send();

        assertThat( smtpServer.getReceivedEmailSize(), is( 1 ) );
        SmtpMessage received = (SmtpMessage) smtpServer.getReceivedEmail().next();
        assertThat( received.getHeaderValue( "From" ), equalTo( "from@qiweb.org" ) );
        assertThat( received.getHeaderValue( "To" ), equalTo( "to@qiweb.org" ) );
        assertThat( received.getHeaderValue( "Subject" ), equalTo( "Simple Email" ) );
    }
}
