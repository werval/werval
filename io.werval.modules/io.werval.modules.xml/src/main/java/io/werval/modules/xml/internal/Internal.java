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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import io.werval.modules.xml.XML;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal internal internal.
 */
public final class Internal
{
    public static final AtomicBoolean EXTERNAL_ENTITIES = new AtomicBoolean( Boolean.FALSE );
    public static final AtomicReference<Resolver> RESOLVER = new AtomicReference<>( EmptyResolver.INSTANCE );

    /* package */ static final Logger LOG = LoggerFactory.getLogger( XML.class.getPackage().getName() );
    /* package */ static final String ACCESS_EXTERNAL_NONE = "";
    /* package */ static final String ACCESS_EXTERNAL_LOCAL_ONLY = "file, jar:file";
    /* package */ static final String ACCESS_EXTERNAL_ALL = "all";

    private Internal()
    {
    }
}
