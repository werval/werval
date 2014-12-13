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
package io.werval.util;

import java.util.function.Predicate;

import static io.werval.util.IllegalArguments.ensureNotNull;

/**
 * Class utilities.
 */
public final class Classes
{
    public static Predicate<Class<?>> equal( Class<?> clazz )
    {
        ensureNotNull( "Class", clazz );
        return (otherClazz) -> clazz.equals( otherClazz );
    }

    public static Predicate<Class<?>> assignable( Class<?> clazz )
    {
        ensureNotNull( "Class", clazz );
        return (otherClazz) -> clazz.isAssignableFrom( otherClazz );
    }

    private Classes()
    {
    }
}
