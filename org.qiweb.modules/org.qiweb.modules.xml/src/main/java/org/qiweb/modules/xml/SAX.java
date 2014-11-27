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
package org.qiweb.modules.xml;

/**
 * SAX.
 */
public interface SAX
{
    /**
     * SAX Features.
     * <p>
     * Feature list can be found
     * <a href="http://www.saxproject.org/apidoc/org/xml/sax/package-summary.html#package_description">here</a>.
     */
    interface Features
    {
        String PREFIX = "http://xml.org/sax/features/";

        String EXTERNAL_GENERAL_ENTITIES = PREFIX + "external-general-entities";
        String EXTERNAL_PARAMETER_ENTITIES = PREFIX + "external-parameter-entities";
        String XML_1_1 = PREFIX + "xml-1.1";
    }
}
