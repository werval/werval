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
package io.werval.modules.smtp.internal;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import io.werval.modules.metrics.Metrics;

/**
 * MultiPartEmail with Metrics.
 */
public class MultiPartEmailMetrics
    extends MultiPartEmail
{
    private final Metrics metrics;

    public MultiPartEmailMetrics( Metrics metrics )
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
            metrics.metrics().meter( "io.werval.modules.smtp.sent" ).mark();
            return messageId;
        }
        catch( Exception ex )
        {
            metrics.metrics().meter( "io.werval.modules.smtp.errors" ).mark();
            throw ex;
        }
    }
}
