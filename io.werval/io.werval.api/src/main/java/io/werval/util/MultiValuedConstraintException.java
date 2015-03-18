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
package io.werval.util;

/**
 * Thrown when one of the {@literal single} prefixed methods of {@link MultiValued} implementations is called
 * and constraints are not met.
 */
public class MultiValuedConstraintException
    extends IllegalArgumentException
{
    private static final long serialVersionUID = 1L;

    /**
     * Create a new MultiValuedConstraintException.
     *
     * @param message Message
     */
    public MultiValuedConstraintException( String message )
    {
        super( message );
    }

    /**
     * Create a new MultiValuedConstraintException.
     * <p>
     * Use given cause message as own message.
     *
     * @param cause Cause
     */
    public MultiValuedConstraintException( Throwable cause )
    {
        super( cause );
    }

    /**
     * Create a new MultiValuedConstraintException.
     *
     * @param message Message
     * @param cause   Cause
     */
    public MultiValuedConstraintException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
