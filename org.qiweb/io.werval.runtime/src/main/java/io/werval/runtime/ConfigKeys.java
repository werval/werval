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
package io.werval.runtime;

/**
 * Werval Runtime Configuration Keys.
 */
public interface ConfigKeys
{
    //
    // Application configuration keys
    //
    String APP_BANNER = "app.banner";
    String APP_ERRORS_RECORD_MAX = "app.errors.record.max";
    String APP_EXECUTORS = "app.executors";
    String APP_EXECUTORS_DEFAULT = "app.executors.default";
    String APP_EXECUTORS_SHUTDOWN_TIMEOUT = "app.executors.shutdown.timeout";
    String APP_GLOBAL = "app.global";
    String APP_LANGS = "app.langs";
    String APP_SECRET = "app.secret";
    String APP_SESSION_COOKIE_NAME = "app.session.cookie.name";
    String APP_SESSION_COOKIE_DOMAIN = "app.session.cookie.domain";
    String APP_SESSION_COOKIE_HTTPONLY = "app.session.cookie.http_only";
    String APP_SESSION_COOKIE_ONLYIFCHANGED = "app.session.cookie.only_if_changed";
    String APP_SESSION_COOKIE_PATH = "app.session.cookie.path";
    String APP_SESSION_COOKIE_SECURE = "app.session.cookie.secure";
    //
    // Framework configuration keys
    //
    String WERVAL_CHARACTER_ENCODING = "qiweb.character_encoding";
    String WERVAL_HTTP_ADDRESS = "qiweb.http.address";
    String WERVAL_HTTP_PORT = "qiweb.http.port";
    String WERVAL_HTTP_ACCEPTORS = "qiweb.http.acceptors";
    String WERVAL_HTTP_IOTHREADS = "qiweb.http.iothreads";
    String WERVAL_HTTP_TIMEOUT_READ = "qiweb.http.timeout.read";
    String WERVAL_HTTP_TIMEOUT_WRITE = "qiweb.http.timeout.write";
    String WERVAL_HTTP_CHUNKSIZE = "qiweb.http.chunksize";
    String WERVAL_HTTP_LOG_LOWLEVEL_ENABLED = "qiweb.http.log.low_level.enabled";
    String WERVAL_HTTP_LOG_LOWLEVEL_LEVEL = "qiweb.http.log.low_level.level";
    String WERVAL_HTTP_REQUESTS_BODY_MAX_SIZE = "qiweb.http.requests.body.max_size";
    String WERVAL_HTTP_REQUESTS_BODY_DISK_THRESHOLD = "qiweb.http.requests.body.disk_threshold";
    String WERVAL_HTTP_FORMS_MULTIVALUED = "qiweb.http.forms.multi_valued";
    String WERVAL_HTTP_HEADERS_MULTIVALUED = "qiweb.http.headers.multi_valued";
    String WERVAL_HTTP_HEADERS_X_FORWARDED_FOR_ENABLED = "qiweb.http.headers.x_forwarded_for.enabled";
    String WERVAL_HTTP_HEADERS_X_FORWARDED_FOR_CHECK = "qiweb.http.headers.x_forwarded_for.check_proxies";
    String WERVAL_HTTP_HEADERS_X_FORWARDED_FOR_TRUSTED = "qiweb.http.headers.x_forwarded_for.trusted_proxies";
    String WERVAL_HTTP_QUERYSTRING_MULTIVALUED = "qiweb.http.query_string.multi_valued";
    String WERVAL_HTTP_UPLOADS_MULTIVALUED = "qiweb.http.uploads.multi_valued";
    String WERVAL_MIMETYPES_SUPPLEMENTARY = "qiweb.mimetypes.supplementary";
    String WERVAL_MIMETYPES_TEXTUAL = "qiweb.mimetypes.textual";
    String WERVAL_ROUTES_IMPORTEDPACKAGES = "qiweb.routes.imported_packages";
    String WERVAL_ROUTES_PARAMETERBINDERS = "qiweb.routes.parameter_binders";
    String WERVAL_SHUTDOWN_QUIETPERIOD = "qiweb.shutdown.quiet_period";
    String WERVAL_SHUTDOWN_TIMEOUT = "qiweb.shutdown.timeout";
    String WERVAL_SHUTDOWN_RETRYAFTER = "qiweb.shutdown.retry_after";
    String WERVAL_TMPDIR = "qiweb.tmpdir";
}
