/*
 * Copyright (c) 2013 the original author or authors
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
package io.werval.api.exceptions;

/**
 * Base exception for all werval errors.
 */
public class WervalException
    extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    /**
     * Create a new WervalException.
     *
     * @param message Message
     */
    public WervalException( String message )
    {
        super( message );
    }

    /**
     * Create a new WervalException.
     * <p>
     * Use given cause message as own message.
     *
     * @param cause Cause
     */
    public WervalException( Throwable cause )
    {
        super( cause.getMessage(), cause );
    }

    /**
     * Create a new WervalException.
     *
     * @param message Message
     * @param cause   Cause
     */
    public WervalException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
