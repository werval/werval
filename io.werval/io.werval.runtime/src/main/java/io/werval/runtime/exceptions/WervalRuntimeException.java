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
package io.werval.runtime.exceptions;

import io.werval.api.exceptions.WervalException;

/**
 * Thrown when an unexpected error occurs inside the Werval Runtime.
 */
public class WervalRuntimeException
    extends WervalException
{
    private static final long serialVersionUID = 1L;

    public WervalRuntimeException( String message )
    {
        super( message );
    }

    public WervalRuntimeException( Throwable cause )
    {
        super( cause );
    }

    public WervalRuntimeException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
