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
package org.qiweb.runtime;

import org.qiweb.spi.ApplicationSPI;
import org.slf4j.Logger;

/**
 * ErrorHandling.
 */
/* package */ final class ErrorHandling
{
    /* package */ static Throwable cleanUpStackTrace( ApplicationSPI application, Logger log, Throwable cause )
    {
        Throwable rootCauseRef;
        try
        {
            if( application.executors().inDefaultExecutor() )
            {
                rootCauseRef = application.global().getRootCause( cause );
            }
            else
            {
                rootCauseRef = application.executors().supplyAsync(
                    () -> application.global().getRootCause( cause )
                ).join();
            }
        }
        catch( Exception ex )
        {
            log.warn(
                "An error occured in Global::getRootCause() method "
                + "and has been added as suppressed exception to the original error stacktrace. Message: {}",
                ex.getMessage(), ex
            );
            cause.addSuppressed( ex );
            rootCauseRef = cause;
        }
        return rootCauseRef;
    }

    private ErrorHandling()
    {
    }
}
