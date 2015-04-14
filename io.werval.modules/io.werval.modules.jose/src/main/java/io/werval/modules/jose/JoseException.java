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
package io.werval.modules.jose;

import io.werval.api.exceptions.WervalException;

/**
 * JoseException.
 * <p>
 * Thrown when something goes bad when processing JOSE entities like JWTs.
 */
public class JoseException
    extends WervalException
{
    public JoseException( String message )
    {
        super( message );
    }

    public JoseException( Throwable cause )
    {
        super( cause );
    }

    public JoseException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
