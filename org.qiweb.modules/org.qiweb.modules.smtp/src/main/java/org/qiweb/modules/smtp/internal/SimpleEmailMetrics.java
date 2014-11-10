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
package org.qiweb.modules.smtp.internal;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.qiweb.modules.metrics.Metrics;

/**
 * SimpleEmail with Metrics.
 */
public class SimpleEmailMetrics
    extends SimpleEmail
{
    private final Metrics metrics;

    public SimpleEmailMetrics( Metrics metrics )
    {
        this.metrics = metrics;
    }

    @Override
    public String sendMimeMessage()
        throws EmailException
    {
        try
        {
            String messageId = super.sendMimeMessage();
            metrics.metrics().meter( "org.qiweb.modules.smtp.sent" ).mark();
            return messageId;
        }
        catch( Exception ex )
        {
            metrics.metrics().meter( "org.qiweb.modules.smtp.errors" ).mark();
            throw ex;
        }
    }
}
