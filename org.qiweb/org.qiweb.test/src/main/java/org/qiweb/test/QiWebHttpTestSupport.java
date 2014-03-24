/*
 * Copyright (c) 2013-2014 the original author or authors
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
package org.qiweb.test;

import org.qiweb.spi.ApplicationSPI;

/**
 * QiWeb HTTP Test Support contract.
 *
 * Provide access to the {@link ApplicationSPI} and 
 */
public interface QiWebHttpTestSupport
{
    /**
     * @return QiWeb Application SPI
     */
    ApplicationSPI application();

    /**
     * @return QiWeb Application HTTP host
     */
    String httpHost();

    /**
     * @return QiWeb Application HTTP port
     */
    int httpPort();

    /**
     * @return QiWeb Application base HTTP URL based on listening address and port Configuration
     */
    String baseHttpUrl();
}