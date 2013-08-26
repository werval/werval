/**
 * Copyright (c) 2013 the original author or authors
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
package org.qiweb.runtime;

/**
 * QiWeb Runtime Configuration Keys.
 */
public interface ConfigKeys
{

    //
    // Application configuration keys
    //
    String APP_GLOBAL = "app.global";
    String APP_MIMETYPES = "app.mimetypes";
    String APP_SECRET = "app.secret";
    String APP_SESSION_COOKIE_NAME = "app.session.cookie.name";
    String APP_SESSION_COOKIE_DOMAIN = "app.session.cookie.domain";
    String APP_SESSION_COOKIE_HTTPONLY = "app.session.cookie.httpOnly";
    String APP_SESSION_COOKIE_ONLYIFCHANGED = "app.session.cookie.onlyIfChanged";
    String APP_SESSION_COOKIE_PATH = "app.session.cookie.path";
    String APP_SESSION_COOKIE_SECURE = "app.session.cookie.secure";
    //
    // Framework configuration keys
    //
    String QIWEB_FS_TEMP = "qiweb.fs.temp";
    String QIWEB_HTTP_ADDRESS = "qiweb.http.address";
    String QIWEB_HTTP_PORT = "qiweb.http.port";
    String QIWEB_HTTP_ACCEPTORS = "qiweb.http.acceptors";
    String QIWEB_HTTP_IOTHREADS = "qiweb.http.iothreads";
    String QIWEB_HTTP_EXECUTORS = "qiweb.http.executors";
    String QIWEB_HTTP_TIMEOUT_READ = "qiweb.http.timeout.read";
    String QIWEB_HTTP_TIMEOUT_WRITE = "qiweb.http.timeout.write";
    String QIWEB_HTTP_CHUNKSIZE = "qiweb.http.chunksize";
    String QIWEB_HTTP_LOG_LOWLEVEL_ENABLED = "qiweb.http.log.low-level.enabled";
    String QIWEB_HTTP_LOG_LOWLEVEL_LEVEL = "qiweb.http.log.low-level.level";
    String QIWEB_ROUTES_IMPORTEDPACKAGES = "qiweb.routes.imported-packages";
    String QIWEB_ROUTES_PARAMETERBINDERS = "qiweb.routes.parameter-binders";
}