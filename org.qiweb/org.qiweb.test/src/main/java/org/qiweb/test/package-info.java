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
/**
 * JUnit Support.
 *
 * <strong>Testing QiWeb Applications</strong>
 * <p>
 * QiWeb Applications tests can be built in two ways:
 * <ul>
 * <li>without any HTTP server running.</li>
 * <li>with an HTTP server running,</li>
 * </ul>
 * The former allow to test the whole stack using an HTTP client in tests.
 * The later allow to test the Application alone.
 * <p>
 * <strong>JUnit Support</strong>
 * <p>
 * JUnit support comes in two flavours:
 * <ul>
 * <li>as JUnit Rules: {@link QiWebRule} and {@link QiWebHttpRule};</li>
 * <li>or as base classes for your JUnit tests to extend: {@link QiWebTest} and {@link QiWebHttpTest}.</li>
 * </ul>
 * You can choose to use whichever suits your needs and habits.
 * <p>
 * Using <code>QiWebRule</code> and <code>QiWebHttpRule</code> is the prefered way of writing QiWeb tests as it
 * don't force you to extend from any base class.
 * <p>
 * QiWeb HTTP JUnit Support has transparent integration with
 * <a href="https://code.google.com/p/rest-assured/" target="_blank">rest-assured</a> if it is detected on the
 * tests classpath.
 * rest-assured base URL is set accordingly to the QiWeb configuration so one can use relative paths when using it.
 */
package org.qiweb.test;
