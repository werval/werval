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
package io.werval.modules.xml.internal;

import javax.xml.stream.XMLResolver;
import javax.xml.transform.URIResolver;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ext.EntityResolver2;

/**
 * XML Resolver for references and entities.
 * <p>
 * Implemented interfaces:
 * <ul>
 * <li>{@link XMLResolver} for {@literal javax.xml.stream} (StAX)</li>
 * <li>{@link org.xml.sax.EntityResolver}, {@link EntityResolver2} for {@literal org.xml.sax} (SAX and SAX2)</li>
 * <li>{@link LSResourceResolver} for {@literal org.w3c.dom} (DOM)</li>
 * <li>{@link URIResolver} for {@literal javax.xml.transform} (XSLT)</li>
 * <li>{@link XMLEntityResolver} for {@literal org.apache.xerces.xni} (Xerces XNI)</li>
 * </ul>
 */
public interface Resolver
    extends XMLResolver, EntityResolver2, LSResourceResolver, URIResolver, XMLEntityResolver
{
}
