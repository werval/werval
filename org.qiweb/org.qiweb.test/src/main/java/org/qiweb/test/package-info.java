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
 * QiWeb JUnit Support.
 * 
 * <p>JUnit support comes in two flavours:</p>
 * <ul>
 *     <li>as a JUnit Rule: {@link QiWebRule} ;</li>
 *     <li>or as a base class for your JUnit tests to extend: {@link QiWebTest}.</li>
 * </ul>
 * <p>You can choose to use whichever suits your needs and habits.</p>
 * <p>
 *     Using the <code>QiWebRule</code> is the prefered way of writing QiWeb tests as it don't force you to extend from
 *     any base class.
 * </p>
 * <p>
 *     QiWeb JUnit Support has transparent integration with
 *     <a href="https://code.google.com/p/rest-assured/" target="_blank">rest-assured</a> if it is detected on the
 *     tests classpath.
 *     rest-assured base URL is set accordingly to the QiWeb configuration so can use relative paths when using it.
 * </p>
 */
package org.qiweb.test;
