/*
 * Copyright (c) 2013-2015 the original author or authors
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

import java.util.function.Function;

import io.werval.api.exceptions.WervalException;

/**
 * Bad Request Exception.
 */
public class BadRequestException
    extends WervalException
{
    private static final long serialVersionUID = 1L;

    public static final Function<String, BadRequestException> BUILDER = msg -> new BadRequestException( msg );

    public BadRequestException( String message )
    {
        super( message );
    }

    public BadRequestException( Throwable cause )
    {
        super( cause );
    }

    public BadRequestException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
