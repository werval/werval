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
package org.qiweb.modules.thymeleaf;

import org.qiweb.test.templates.TemplatesTest;

import static org.qiweb.api.util.Charsets.UTF_8;
import static org.qiweb.api.util.InputStreams.BUF_SIZE_4K;
import static org.qiweb.api.util.InputStreams.readAllAsString;

/**
 * Thymeleaf Templates Test.
 */
public class ThymeleafTemplatesTest
    extends TemplatesTest
{
    @Override
    protected String templateName()
    {
        return "home.html";
    }

    @Override
    protected String templateContent()
    {
        return readAllAsString( getClass().getResourceAsStream( "/views/home.html" ), BUF_SIZE_4K, UTF_8 );
    }
}
