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
package io.werval.test;

import io.werval.spi.ApplicationSPI;
import io.werval.spi.http.HttpBuildersSPI;

/**
 * Werval Test Support contract.
 * <p>
 * Provide access to the {@link ApplicationSPI} and {@link HttpBuildersSPI}.
 */
public interface WervalTestSupport
    extends HttpBuildersSPI
{
    /**
     * @return Werval Application SPI
     */
    ApplicationSPI application();
}
