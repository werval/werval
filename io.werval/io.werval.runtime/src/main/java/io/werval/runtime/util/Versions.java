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
package io.werval.runtime.util;

import java.util.Arrays;
import java.util.stream.Stream;

import io.werval.runtime.exceptions.WervalRuntimeException;

/**
 * Werval Versions Utilities.
 */
public final class Versions
{
    /**
     * Ensure that Werval API, SPI and Runtime versions are equals.
     */
    public static void ensureComponentsVersions()
    {
        Stream<String> versions = Arrays.asList(
            io.werval.api.BuildVersion.VERSION, io.werval.spi.BuildVersion.VERSION
        ).stream();
        if( !versions.allMatch( v -> v.equals( io.werval.runtime.BuildVersion.VERSION ) ) )
        {
            throw new WervalRuntimeException(
                String.format(
                    "Werval Core classpath mismatch: io.werval.api:%s io.werval.spi:%s io.werval.runtime:%s",
                    io.werval.api.BuildVersion.VERSION,
                    io.werval.spi.BuildVersion.VERSION,
                    io.werval.runtime.BuildVersion.VERSION
                )
            );
        }
    }

    private Versions()
    {
    }
}
