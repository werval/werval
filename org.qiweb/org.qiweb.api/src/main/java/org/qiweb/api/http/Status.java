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
package org.qiweb.api.http;

import java.io.Serializable;
import org.qiweb.api.util.Charsets;

/**
 * HTTP Status.
 */
public final class Status
    implements Comparable<Status>, Serializable
{
    /**
     * 100 Continue.
     */
    private static final int CONTINUE_CODE = 100;

    /**
     * 100 Continue.
     */
    public static final Status CONTINUE;

    /**
     * 101 Switching Protocols.
     */
    public static final int SWITCHING_PROTOCOLS_CODE = 101;

    /**
     * 101 Switching Protocols.
     */
    public static final Status SWITCHING_PROTOCOLS;

    /**
     * 102 Processing (WebDAV, RFC2518).
     */
    public static final int PROCESSING_CODE = 102;

    /**
     * 102 Processing (WebDAV, RFC2518).
     */
    public static final Status PROCESSING;

    /**
     * 200 OK.
     */
    public static final int OK_CODE = 200;

    /**
     * 200 OK.
     */
    public static final Status OK;

    /**
     * 201 Created.
     */
    public static final int CREATED_CODE = 201;

    /**
     * 201 Created.
     */
    public static final Status CREATED;

    /**
     * 202 Accepted.
     */
    public static final int ACCEPTED_CODE = 202;

    /**
     * 202 Accepted.
     */
    public static final Status ACCEPTED;

    /**
     * 203 Non-Authoritative Information (since HTTP/1.1).
     */
    public static final int NON_AUTHORITATIVE_INFORMATION_CODE = 203;

    /**
     * 203 Non-Authoritative Information (since HTTP/1.1).
     */
    public static final Status NON_AUTHORITATIVE_INFORMATION;

    /**
     * 204 No Content.
     */
    public static final int NO_CONTENT_CODE = 204;

    /**
     * 204 No Content.
     */
    public static final Status NO_CONTENT;

    /**
     * 205 Reset Content.
     */
    public static final int RESET_CONTENT_CODE = 205;

    /**
     * 205 Reset Content.
     */
    public static final Status RESET_CONTENT;

    /**
     * 206 Partial Content.
     */
    public static final int PARTIAL_CONTENT_CODE = 206;

    /**
     * 206 Partial Content.
     */
    public static final Status PARTIAL_CONTENT;

    /**
     * 207 Multi-Status (WebDAV, RFC2518).
     */
    public static final int MULTI_STATUS_CODE = 207;

    /**
     * 207 Multi-Status (WebDAV, RFC2518).
     */
    public static final Status MULTI_STATUS;

    /**
     * 300 Multiple Choices.
     */
    public static final int MULTIPLE_CHOICES_CODE = 300;

    /**
     * 300 Multiple Choices.
     */
    public static final Status MULTIPLE_CHOICES;

    /**
     * 301 Moved Permanently.
     */
    public static final int MOVED_PERMANENTLY_CODE = 301;

    /**
     * 301 Moved Permanently.
     */
    public static final Status MOVED_PERMANENTLY;

    /**
     * 302 Found.
     */
    public static final int FOUND_CODE = 302;

    /**
     * 302 Found.
     */
    public static final Status FOUND;

    /**
     * 303 See Other (since HTTP/1.1).
     */
    public static final int SEE_OTHER_CODE = 303;

    /**
     * 303 See Other (since HTTP/1.1).
     */
    public static final Status SEE_OTHER;

    /**
     * 304 Not Modified.
     */
    public static final int NOT_MODIFIED_CODE = 304;

    /**
     * 304 Not Modified.
     */
    public static final Status NOT_MODIFIED;

    /**
     * 305 Use Proxy (since HTTP/1.1).
     */
    public static final int USE_PROXY_CODE = 305;

    /**
     * 305 Use Proxy (since HTTP/1.1).
     */
    public static final Status USE_PROXY;

    /**
     * 307 Temporary Redirect (since HTTP/1.1).
     */
    public static final int TEMPORARY_REDIRECT_CODE = 307;

    /**
     * 307 Temporary Redirect (since HTTP/1.1).
     */
    public static final Status TEMPORARY_REDIRECT;

    /**
     * 400 Bad Request.
     */
    public static final int BAD_REQUEST_CODE = 400;

    /**
     * 400 Bad Request.
     */
    public static final Status BAD_REQUEST;

    /**
     * 401 Unauthorized.
     */
    public static final int UNAUTHORIZED_CODE = 401;

    /**
     * 401 Unauthorized.
     */
    public static final Status UNAUTHORIZED;

    /**
     * 402 Payment Required.
     */
    public static final int PAYMENT_REQUIRED_CODE = 402;

    /**
     * 402 Payment Required.
     */
    public static final Status PAYMENT_REQUIRED;

    /**
     * 403 Forbidden.
     */
    public static final int FORBIDDEN_CODE = 403;

    /**
     * 403 Forbidden.
     */
    public static final Status FORBIDDEN;

    /**
     * 404 Not Found.
     */
    public static final int NOT_FOUND_CODE = 404;

    /**
     * 404 Not Found.
     */
    public static final Status NOT_FOUND;

    /**
     * 405 Method Not Allowed.
     */
    public static final int METHOD_NOT_ALLOWED_CODE = 405;

    /**
     * 405 Method Not Allowed.
     */
    public static final Status METHOD_NOT_ALLOWED;

    /**
     * 406 Not Acceptable.
     */
    public static final int NOT_ACCEPTABLE_CODE = 406;

    /**
     * 406 Not Acceptable.
     */
    public static final Status NOT_ACCEPTABLE;

    /**
     * 407 Proxy Authentication Required.
     */
    public static final int PROXY_AUTHENTICATION_REQUIRED_CODE = 407;

    /**
     * 407 Proxy Authentication Required.
     */
    public static final Status PROXY_AUTHENTICATION_REQUIRED;

    /**
     * 408 Request Timeout.
     */
    public static final int REQUEST_TIMEOUT_CODE = 408;

    /**
     * 408 Request Timeout.
     */
    public static final Status REQUEST_TIMEOUT;

    /**
     * 409 Conflict.
     */
    public static final int CONFLICT_CODE = 409;

    /**
     * 409 Conflict.
     */
    public static final Status CONFLICT;

    /**
     * 410 Gone.
     */
    public static final int GONE_CODE = 410;

    /**
     * 410 Gone.
     */
    public static final Status GONE;

    /**
     * 411 Length Required.
     */
    public static final int LENGTH_REQUIRED_CODE = 411;

    /**
     * 411 Length Required.
     */
    public static final Status LENGTH_REQUIRED;

    /**
     * 412 Precondition Failed.
     */
    public static final int PRECONDITION_FAILED_CODE = 412;

    /**
     * 412 Precondition Failed.
     */
    public static final Status PRECONDITION_FAILED;

    /**
     * 413 Request Entity Too Large.
     */
    public static final int REQUEST_ENTITY_TOO_LARGE_CODE = 413;

    /**
     * 413 Request Entity Too Large.
     */
    public static final Status REQUEST_ENTITY_TOO_LARGE;

    /**
     * 414 Request-URI Too Long.
     */
    public static final int REQUEST_URI_TOO_LONG_CODE = 414;

    /**
     * 414 Request-URI Too Long.
     */
    public static final Status REQUEST_URI_TOO_LONG;

    /**
     * 415 Unsupported Media Type.
     */
    public static final int UNSUPPORTED_MEDIA_TYPE_CODE = 415;

    /**
     * 415 Unsupported Media Type.
     */
    public static final Status UNSUPPORTED_MEDIA_TYPE;

    /**
     * 416 Requested Range Not Satisfiable.
     */
    public static final int REQUESTED_RANGE_NOT_SATISFIABLE_CODE = 416;

    /**
     * 416 Requested Range Not Satisfiable.
     */
    public static final Status REQUESTED_RANGE_NOT_SATISFIABLE;

    /**
     * 417 Expectation Failed.
     */
    public static final int EXPECTATION_FAILED_CODE = 417;

    /**
     * 417 Expectation Failed.
     */
    public static final Status EXPECTATION_FAILED;

    /**
     * 422 Unprocessable Entity (WebDAV, RFC4918).
     */
    public static final int UNPROCESSABLE_ENTITY_CODE = 422;

    /**
     * 422 Unprocessable Entity (WebDAV, RFC4918).
     */
    public static final Status UNPROCESSABLE_ENTITY;

    /**
     * 423 Locked (WebDAV, RFC4918).
     */
    public static final int LOCKED_CODE = 423;

    /**
     * 423 Locked (WebDAV, RFC4918).
     */
    public static final Status LOCKED;

    /**
     * 424 Failed Dependency (WebDAV, RFC4918).
     */
    public static final int FAILED_DEPENDENCY_CODE = 424;

    /**
     * 424 Failed Dependency (WebDAV, RFC4918).
     */
    public static final Status FAILED_DEPENDENCY;

    /**
     * 425 Unordered Collection (WebDAV, RFC3648).
     */
    public static final int UNORDERED_COLLECTION_CODE = 425;

    /**
     * 425 Unordered Collection (WebDAV, RFC3648).
     */
    public static final Status UNORDERED_COLLECTION;

    /**
     * 426 Upgrade Required (RFC2817).
     */
    public static final int UPGRADE_REQUIRED_CODE = 426;

    /**
     * 426 Upgrade Required (RFC2817).
     */
    public static final Status UPGRADE_REQUIRED;

    /**
     * 428 Precondition Required (RFC6585).
     */
    public static final int PRECONDITION_REQUIRED_CODE = 428;

    /**
     * 428 Precondition Required (RFC6585).
     */
    public static final Status PRECONDITION_REQUIRED;

    /**
     * 429 Too Many Requests (RFC6585).
     */
    public static final int TOO_MANY_REQUESTS_CODE = 429;

    /**
     * 429 Too Many Requests (RFC6585).
     */
    public static final Status TOO_MANY_REQUESTS;

    /**
     * 431 Request Header Fields Too Large (RFC6585).
     */
    public static final int REQUEST_HEADER_FIELDS_TOO_LARGE_CODE = 431;

    /**
     * 431 Request Header Fields Too Large (RFC6585).
     */
    public static final Status REQUEST_HEADER_FIELDS_TOO_LARGE;

    /**
     * 500 Internal Server Error.
     */
    public static final int INTERNAL_SERVER_ERROR_CODE = 500;

    /**
     * 500 Internal Server Error.
     */
    public static final Status INTERNAL_SERVER_ERROR;

    /**
     * 501 Not Implemented.
     */
    public static final int NOT_IMPLEMENTED_CODE = 501;

    /**
     * 501 Not Implemented.
     */
    public static final Status NOT_IMPLEMENTED;

    /**
     * 502 Bad Gateway.
     */
    public static final int BAD_GATEWAY_CODE = 502;

    /**
     * 502 Bad Gateway.
     */
    public static final Status BAD_GATEWAY;

    /**
     * 503 Service Unavailable.
     */
    public static final int SERVICE_UNAVAILABLE_CODE = 503;

    /**
     * 503 Service Unavailable.
     */
    public static final Status SERVICE_UNAVAILABLE;

    /**
     * 504 Gateway Timeout.
     */
    public static final int GATEWAY_TIMEOUT_CODE = 504;

    /**
     * 504 Gateway Timeout.
     */
    public static final Status GATEWAY_TIMEOUT;

    /**
     * 505 HTTP Version Not Supported.
     */
    public static final int HTTP_VERSION_NOT_SUPPORTED_CODE = 505;

    /**
     * 505 HTTP Version Not Supported.
     */
    public static final Status HTTP_VERSION_NOT_SUPPORTED;

    /**
     * 506 Variant Also Negotiates (RFC2295).
     */
    public static final int VARIANT_ALSO_NEGOTIATES_CODE = 506;

    /**
     * 506 Variant Also Negotiates (RFC2295).
     */
    public static final Status VARIANT_ALSO_NEGOTIATES;

    /**
     * 507 Insufficient Storage (WebDAV, RFC4918).
     */
    public static final int INSUFFICIENT_STORAGE_CODE = 507;

    /**
     * 507 Insufficient Storage (WebDAV, RFC4918).
     */
    public static final Status INSUFFICIENT_STORAGE;

    /**
     * 510 Not Extended (RFC2774).
     */
    public static final int NOT_EXTENDED_CODE = 510;

    /**
     * 510 Not Extended (RFC2774).
     */
    public static final Status NOT_EXTENDED;

    /**
     * 511 Network Authentication Required (RFC6585).
     */
    public static final int NETWORK_AUTHENTICATION_REQUIRED_CODE = 511;

    /**
     * 511 Network Authentication Required (RFC6585).
     */
    public static final Status NETWORK_AUTHENTICATION_REQUIRED;

    static
    {
        // 1xx
        CONTINUE = new Status(
            CONTINUE_CODE,
            "Continue"
        );
        SWITCHING_PROTOCOLS = new Status(
            SWITCHING_PROTOCOLS_CODE,
            "Switching Protocols"
        );
        PROCESSING = new Status(
            PROCESSING_CODE,
            "Processing"
        );
        // 2xx
        OK = new Status(
            OK_CODE,
            "OK"
        );
        CREATED = new Status(
            CREATED_CODE,
            "Created"
        );
        ACCEPTED = new Status(
            ACCEPTED_CODE,
            "Accepted"
        );
        NON_AUTHORITATIVE_INFORMATION = new Status(
            NON_AUTHORITATIVE_INFORMATION_CODE,
            "Non-Authoritative Information"
        );
        NO_CONTENT = new Status(
            NO_CONTENT_CODE,
            "No Content"
        );
        RESET_CONTENT = new Status(
            RESET_CONTENT_CODE,
            "Reset Content"
        );
        PARTIAL_CONTENT = new Status(
            PARTIAL_CONTENT_CODE,
            "Partial Content"
        );
        MULTI_STATUS = new Status(
            MULTI_STATUS_CODE,
            "Multi-Status"
        );
        // 3xx
        MULTIPLE_CHOICES = new Status(
            MULTIPLE_CHOICES_CODE,
            "Multiple Choices"
        );
        FOUND = new Status(
            FOUND_CODE,
            "Found"
        );
        MOVED_PERMANENTLY = new Status(
            MOVED_PERMANENTLY_CODE,
            "Moved Permanently"
        );
        SEE_OTHER = new Status(
            SEE_OTHER_CODE,
            "See Other"
        );
        NOT_MODIFIED = new Status(
            NOT_MODIFIED_CODE,
            "Not Modified"
        );
        USE_PROXY = new Status(
            USE_PROXY_CODE,
            "Use Proxy"
        );
        TEMPORARY_REDIRECT = new Status(
            TEMPORARY_REDIRECT_CODE,
            "Temporary Redirect"
        );
        // 4xx
        BAD_REQUEST = new Status(
            BAD_REQUEST_CODE,
            "Bad Request"
        );
        UNAUTHORIZED = new Status(
            UNAUTHORIZED_CODE,
            "Unauthorized"
        );
        PAYMENT_REQUIRED = new Status(
            PAYMENT_REQUIRED_CODE,
            "Payment Required"
        );
        FORBIDDEN = new Status(
            FORBIDDEN_CODE,
            "Forbidden"
        );
        NOT_FOUND = new Status(
            NOT_FOUND_CODE,
            "Not Found"
        );
        METHOD_NOT_ALLOWED = new Status(
            METHOD_NOT_ALLOWED_CODE,
            "Method Not Allowed"
        );
        NOT_ACCEPTABLE = new Status(
            NOT_ACCEPTABLE_CODE,
            "Not Acceptable"
        );
        PROXY_AUTHENTICATION_REQUIRED = new Status(
            PROXY_AUTHENTICATION_REQUIRED_CODE,
            "Proxy Authentication Required"
        );
        REQUEST_TIMEOUT = new Status(
            REQUEST_TIMEOUT_CODE,
            "Request Timeout"
        );
        CONFLICT = new Status(
            CONFLICT_CODE,
            "Conflict"
        );
        GONE = new Status(
            GONE_CODE,
            "Gone"
        );
        LENGTH_REQUIRED = new Status(
            LENGTH_REQUIRED_CODE,
            "Length Required"
        );
        PRECONDITION_FAILED = new Status(
            PRECONDITION_FAILED_CODE,
            "Precondition Failed"
        );
        REQUEST_ENTITY_TOO_LARGE = new Status(
            REQUEST_ENTITY_TOO_LARGE_CODE,
            "Request Entity Too Large"
        );
        REQUEST_URI_TOO_LONG = new Status(
            REQUEST_URI_TOO_LONG_CODE,
            "Request-URI Too Long"
        );
        UNSUPPORTED_MEDIA_TYPE = new Status(
            UNSUPPORTED_MEDIA_TYPE_CODE,
            "Unsupported Media Type"
        );
        REQUESTED_RANGE_NOT_SATISFIABLE = new Status(
            REQUESTED_RANGE_NOT_SATISFIABLE_CODE,
            "Requested Range Not Satisfiable"
        );
        EXPECTATION_FAILED = new Status(
            EXPECTATION_FAILED_CODE,
            "Expectation Failed"
        );
        UNPROCESSABLE_ENTITY = new Status(
            UNPROCESSABLE_ENTITY_CODE,
            "Unprocessable Entity"
        );
        LOCKED = new Status(
            LOCKED_CODE,
            "Locked"
        );
        FAILED_DEPENDENCY = new Status(
            FAILED_DEPENDENCY_CODE,
            "Failed Dependency"
        );
        UNORDERED_COLLECTION = new Status(
            UNORDERED_COLLECTION_CODE,
            "Unordered Collection"
        );
        UPGRADE_REQUIRED = new Status(
            UPGRADE_REQUIRED_CODE,
            "Upgrade Required"
        );
        PRECONDITION_REQUIRED = new Status(
            PRECONDITION_REQUIRED_CODE,
            "Precondition Required"
        );
        TOO_MANY_REQUESTS = new Status(
            TOO_MANY_REQUESTS_CODE,
            "Too Many Requests"
        );
        REQUEST_HEADER_FIELDS_TOO_LARGE = new Status(
            REQUEST_HEADER_FIELDS_TOO_LARGE_CODE,
            "Request Header Fields Too Large"
        );
        // 5xx
        INTERNAL_SERVER_ERROR = new Status(
            INTERNAL_SERVER_ERROR_CODE,
            "Internal Server Error"
        );
        NOT_IMPLEMENTED = new Status(
            NOT_IMPLEMENTED_CODE,
            "Not Implemented"
        );
        BAD_GATEWAY = new Status(
            BAD_GATEWAY_CODE,
            "Bad Gateway"
        );
        SERVICE_UNAVAILABLE = new Status(
            SERVICE_UNAVAILABLE_CODE,
            "Service Unavailable"
        );
        GATEWAY_TIMEOUT = new Status(
            GATEWAY_TIMEOUT_CODE,
            "Gateway Timeout"
        );
        HTTP_VERSION_NOT_SUPPORTED = new Status(
            HTTP_VERSION_NOT_SUPPORTED_CODE,
            "HTTP Version Not Supported"
        );
        VARIANT_ALSO_NEGOTIATES = new Status(
            VARIANT_ALSO_NEGOTIATES_CODE,
            "Variant Also Negotiates"
        );
        INSUFFICIENT_STORAGE = new Status(
            INSUFFICIENT_STORAGE_CODE,
            "Insufficient Storage"
        );
        NOT_EXTENDED = new Status(
            NOT_EXTENDED_CODE,
            "Not Extended"
        );
        NETWORK_AUTHENTICATION_REQUIRED = new Status(
            NETWORK_AUTHENTICATION_REQUIRED_CODE,
            "Network Authentication Required"
        );
    }

    /**
     * Returns the {@link Status} represented by the specified code.
     *
     * If the specified code is a standard HTTP getStatus code, a cached instance will be returned.
     * Otherwise, a new instance will be returned.
     *
     * @param code Status code integer value
     *
     * @return Status instance
     */
    public static Status valueOf( final int code )
    {
        switch( code )
        {
            case CONTINUE_CODE:
                return CONTINUE;
            case SWITCHING_PROTOCOLS_CODE:
                return SWITCHING_PROTOCOLS;
            case PROCESSING_CODE:
                return PROCESSING;
            case OK_CODE:
                return OK;
            case CREATED_CODE:
                return CREATED;
            case ACCEPTED_CODE:
                return ACCEPTED;
            case NON_AUTHORITATIVE_INFORMATION_CODE:
                return NON_AUTHORITATIVE_INFORMATION;
            case NO_CONTENT_CODE:
                return NO_CONTENT;
            case RESET_CONTENT_CODE:
                return RESET_CONTENT;
            case PARTIAL_CONTENT_CODE:
                return PARTIAL_CONTENT;
            case MULTI_STATUS_CODE:
                return MULTI_STATUS;
            case MULTIPLE_CHOICES_CODE:
                return MULTIPLE_CHOICES;
            case MOVED_PERMANENTLY_CODE:
                return MOVED_PERMANENTLY;
            case FOUND_CODE:
                return FOUND;
            case SEE_OTHER_CODE:
                return SEE_OTHER;
            case NOT_MODIFIED_CODE:
                return NOT_MODIFIED;
            case USE_PROXY_CODE:
                return USE_PROXY;
            case TEMPORARY_REDIRECT_CODE:
                return TEMPORARY_REDIRECT;
            case BAD_REQUEST_CODE:
                return BAD_REQUEST;
            case UNAUTHORIZED_CODE:
                return UNAUTHORIZED;
            case PAYMENT_REQUIRED_CODE:
                return PAYMENT_REQUIRED;
            case FORBIDDEN_CODE:
                return FORBIDDEN;
            case NOT_FOUND_CODE:
                return NOT_FOUND;
            case METHOD_NOT_ALLOWED_CODE:
                return METHOD_NOT_ALLOWED;
            case NOT_ACCEPTABLE_CODE:
                return NOT_ACCEPTABLE;
            case PROXY_AUTHENTICATION_REQUIRED_CODE:
                return PROXY_AUTHENTICATION_REQUIRED;
            case REQUEST_TIMEOUT_CODE:
                return REQUEST_TIMEOUT;
            case CONFLICT_CODE:
                return CONFLICT;
            case GONE_CODE:
                return GONE;
            case LENGTH_REQUIRED_CODE:
                return LENGTH_REQUIRED;
            case PRECONDITION_FAILED_CODE:
                return PRECONDITION_FAILED;
            case REQUEST_ENTITY_TOO_LARGE_CODE:
                return REQUEST_ENTITY_TOO_LARGE;
            case REQUEST_URI_TOO_LONG_CODE:
                return REQUEST_URI_TOO_LONG;
            case UNSUPPORTED_MEDIA_TYPE_CODE:
                return UNSUPPORTED_MEDIA_TYPE;
            case REQUESTED_RANGE_NOT_SATISFIABLE_CODE:
                return REQUESTED_RANGE_NOT_SATISFIABLE;
            case EXPECTATION_FAILED_CODE:
                return EXPECTATION_FAILED;
            case UNPROCESSABLE_ENTITY_CODE:
                return UNPROCESSABLE_ENTITY;
            case LOCKED_CODE:
                return LOCKED;
            case FAILED_DEPENDENCY_CODE:
                return FAILED_DEPENDENCY;
            case UNORDERED_COLLECTION_CODE:
                return UNORDERED_COLLECTION;
            case UPGRADE_REQUIRED_CODE:
                return UPGRADE_REQUIRED;
            case PRECONDITION_REQUIRED_CODE:
                return PRECONDITION_REQUIRED;
            case TOO_MANY_REQUESTS_CODE:
                return TOO_MANY_REQUESTS;
            case REQUEST_HEADER_FIELDS_TOO_LARGE_CODE:
                return REQUEST_HEADER_FIELDS_TOO_LARGE;
            case INTERNAL_SERVER_ERROR_CODE:
                return INTERNAL_SERVER_ERROR;
            case NOT_IMPLEMENTED_CODE:
                return NOT_IMPLEMENTED;
            case BAD_GATEWAY_CODE:
                return BAD_GATEWAY;
            case SERVICE_UNAVAILABLE_CODE:
                return SERVICE_UNAVAILABLE;
            case GATEWAY_TIMEOUT_CODE:
                return GATEWAY_TIMEOUT;
            case HTTP_VERSION_NOT_SUPPORTED_CODE:
                return HTTP_VERSION_NOT_SUPPORTED;
            case VARIANT_ALSO_NEGOTIATES_CODE:
                return VARIANT_ALSO_NEGOTIATES;
            case INSUFFICIENT_STORAGE_CODE:
                return INSUFFICIENT_STORAGE;
            case NOT_EXTENDED_CODE:
                return NOT_EXTENDED;
            case NETWORK_AUTHENTICATION_REQUIRED_CODE:
                return NETWORK_AUTHENTICATION_REQUIRED;
            default:
                return new Status( code, StatusClass.valueOf( code ).reasonPhrase() + " (" + code + ')' );
        }
    }

    private final int code;
    private final StatusClass statusClass;
    private final String reasonPhrase;
    private final String string;
    private final byte[] bytes;

    /**
     * Create new Status.
     *
     * @param code         Status code
     * @param reasonPhrase Reason phrase
     */
    public Status( final int code, final String reasonPhrase )
    {
        if( code < 0 )
        {
            throw new IllegalArgumentException( "code: " + code + " (expected: 0+)" );
        }
        if( reasonPhrase == null )
        {
            throw new NullPointerException( "reasonPhrase" );
        }
        for( int idx = 0; idx < reasonPhrase.length(); )
        {
            int c = reasonPhrase.codePointAt( idx );
            // Check prohibited characters.
            if( c == '\n' || c == '\r' )
            {
                throw new IllegalArgumentException(
                    "reasonPhrase contains one of the following prohibited characters: \\r\\n: " + reasonPhrase
                );
            }
            idx += Character.charCount( c );
        }
        this.code = code;
        this.statusClass = StatusClass.valueOf( code );
        this.reasonPhrase = reasonPhrase;
        this.string = new StringBuilder( reasonPhrase.length() + 4 )
            .append( code ).append( ' ' ).append( reasonPhrase )
            .toString();
        this.bytes = this.string.getBytes( Charsets.US_ASCII );
    }

    /**
     * @return Status code
     */
    public int code()
    {
        return code;
    }

    /**
     * @return Status class
     */
    public StatusClass statusClass()
    {
        return statusClass;
    }

    /**
     * @return Reason phrase
     */
    public String reasonPhrase()
    {
        return reasonPhrase;
    }

    /**
     * @return Status line bytes ready to be sent to clients
     */
    public byte[] bytes()
    {
        return bytes;
    }

    @Override
    public int hashCode()
    {
        return code();
    }

    @Override
    public boolean equals( final Object o )
    {
        if( !( o instanceof Status ) )
        {
            return false;
        }
        return code() == ( (Status) o ).code();
    }

    @Override
    public int compareTo( final Status o )
    {
        return code() - o.code();
    }

    @Override
    public String toString()
    {
        return string;
    }
}
