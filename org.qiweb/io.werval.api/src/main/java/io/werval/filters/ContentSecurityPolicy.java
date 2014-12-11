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
package io.werval.filters;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import io.werval.api.Config;
import io.werval.api.context.Context;
import io.werval.api.filters.FilterChain;
import io.werval.api.filters.FilterWith;
import io.werval.api.outcomes.Outcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.werval.util.Strings.EMPTY;
import static io.werval.util.Strings.hasTextOrNull;
import static io.werval.api.context.CurrentContext.application;
import static io.werval.api.context.CurrentContext.outcomes;
import static io.werval.api.context.CurrentContext.request;
import static io.werval.api.http.Method.POST;
import static io.werval.api.mime.MimeTypes.APPLICATION_JSON;

/**
 * Content-Security-Policy Annotation.
 * <p>
 * Adds Content Security Policy (CSP) header to HTTP response.
 * <p>
 * See the <a href="http://www.w3.org/TR/CSP/">W3C Candidate Recommendation</a> and the
 * <a href="https://developer.mozilla.org/en-US/docs/Web/Security/CSP">MDN CSP documentation</a>.
 * <p>
 * Sets {@literal Content-Security-Policy}, {@literal X-Content-Security-Policy} and {@literal X-WebKit-CSP} headers.
 * Using their {@literal -Report-Only} flavour when needed.
 * <p>
 * In the wild, CSP reporting is pretty useless as user's bookmarklets, extensions may create a lot of false positives.
 * However this can prove really useful at development or QA stage.
 * To support this, the {@link ViolationLogger} controller comes as a simple violation logging facility.
 * Add the following route to your Application to get all violations loggued, assuming your {@literal report-uri} CSP
 * directive is set to {@literal /csp-violations}.
 * <blockquote>
 * <pre>POST /csp-violations io.werval.filters.ContentSecurityPolicy$ViolationLogger.logViolation</pre>
 * </blockquote>
 * Logging level is {@literal WARN} by default, this can be changed by setting the
 * {@literal qiweb.filters.csp.report_log_level} configuration property to {@literal error}, {@literal warn},
 * {@literal info}, {@literal debug} or {@literal trace} value.
 *
 * @navassoc 1 apply 1 Filter
 */
@FilterWith( ContentSecurityPolicy.Filter.class )
@Target( { ElementType.METHOD, ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
@Documented
@Repeatable( ContentSecurityPolicy.Repeat.class )
public @interface ContentSecurityPolicy
{
    String policy() default EMPTY;

    boolean reportOnly() default false;

    /**
     * Content-Security-Policy Filter.
     */
    public static class Filter
        implements io.werval.api.filters.Filter<ContentSecurityPolicy>
    {
        @Override
        public CompletableFuture<Outcome> filter(
            FilterChain chain,
            Context context,
            Optional<ContentSecurityPolicy> annotation
        )
        {
            return chain.next( context ).thenApply(
                (outcome) ->
                {
                    Config config = context.application().config();
                    String value = annotation.map(
                        annot -> hasTextOrNull( annot.policy() )
                    ).orElse(
                        config.string( "qiweb.filters.csp.policy" )
                    );
                    boolean reportOnly = annotation.map(
                        annot -> annot.reportOnly() ? true : null
                    ).orElse(
                        config.bool( "qiweb.filters.csp.report_only" )
                    );
                    outcome.responseHeader().headers().withSingle(
                        reportOnly ? "Content-Security-Policy-Report-Only" : "Content-Security-Policy",
                        value
                    );
                    outcome.responseHeader().headers().withSingle(
                        reportOnly ? "X-Content-Security-Policy-Report-Only" : "X-Content-Security-Policy",
                        value
                    );
                    outcome.responseHeader().headers().withSingle(
                        reportOnly ? "X-WebKit-CSP-Report-Only" : "X-WebKit-CSP",
                        value
                    );
                    return outcome;
                }
            );
        }
    }

    /**
     * CSP Violation Logger Controller.
     *
     * @navassoc 1 works-with 1 Filter
     */
    public static class ViolationLogger
    {
        private static final Logger LOG = LoggerFactory.getLogger( ViolationLogger.class );

        /**
         * HTTP Interaction that logs CSP Violations.
         *
         * @return {@literal 400 Bad Request} if the request is not a {@literal POST} of {@literal application/json},
         *         {@literal 204 No Content} uppon success.
         */
        public Outcome logViolation()
        {
            if( !POST.equals( request().method() ) || !APPLICATION_JSON.equals( request().contentType() ) )
            {
                return outcomes().badRequest().build();
            }
            String violationReport = request().body().asString();
            switch( application().config().string( "qiweb.filters.csp.report_log_level" ).toLowerCase( Locale.US ) )
            {
                case "error":
                    LOG.error( violationReport );
                    break;
                case "info":
                    LOG.info( violationReport );
                    break;
                case "debug":
                    LOG.debug( violationReport );
                    break;
                case "trace":
                    LOG.trace( violationReport );
                    break;
                case "warn":
                default:
                    LOG.warn( violationReport );
            }
            return outcomes().noContent().build();
        }
    }

    /**
     * ContentSecurityPolicy repeatable annotation support.
     *
     * @navassoc 1 repeat * ContentSecurityPolicy
     */
    @Retention( RetentionPolicy.RUNTIME )
    @Target( { ElementType.METHOD, ElementType.TYPE } )
    @Inherited
    @Documented
    public static @interface Repeat
    {
        ContentSecurityPolicy[] value();
    }
}
