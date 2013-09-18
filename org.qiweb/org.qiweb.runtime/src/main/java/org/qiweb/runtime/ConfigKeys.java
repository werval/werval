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
    String APP_ERRORS_RECORD_MAX = "app.errors.record.max";
    String APP_GLOBAL = "app.global";
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
    String QIWEB_CHARACTER_ENCODING = "qiweb.character-encoding";
    String QIWEB_FS_TEMP = "qiweb.fs.temp";
    String QIWEB_MIMETYPES_SUPPLEMENTARY = "qiweb.mimetypes.supplementary";
    String QIWEB_MIMETYPES_TEXTUAL = "qiweb.mimetypes.textual";
    String QIWEB_SHUTDOWN_QUIETPERIOD = "qiweb.shutdown.quiet-period";
    String QIWEB_SHUTDOWN_TIMEOUT = "qiweb.shutdown.timeout";
    String QIWEB_SHUTDOWN_RETRYAFTER = "qiweb.shutdown.retry-after";
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
    String QIWEB_HTTP_FORMS_MULTIVALUED = "qiweb.http.forms.multi-valued";
    String QIWEB_HTTP_HEADERS_MULTIVALUED = "qiweb.http.headers.multi-valued";
    String QIWEB_HTTP_QUERYSTRING_MULTIVALUED = "qiweb.http.query-string.multi-valued";
    String QIWEB_HTTP_UPLOADS_MULTIVALUED = "qiweb.http.uploads.multi-valued";
    String QIWEB_ROUTES_IMPORTEDPACKAGES = "qiweb.routes.imported-packages";
    String QIWEB_ROUTES_PARAMETERBINDERS = "qiweb.routes.parameter-binders";
}
