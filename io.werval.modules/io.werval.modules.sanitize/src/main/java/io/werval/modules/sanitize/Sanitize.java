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
package io.werval.modules.sanitize;

import com.codahale.metrics.Timer;
import com.google.json.JsonSanitizer;
import io.werval.api.i18n.Lang;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.owasp.encoder.Encode;
import org.owasp.validator.css.CssScanner;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.InternalPolicy;
import org.owasp.validator.html.Policy;
import io.werval.modules.metrics.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.werval.util.IllegalArguments.ensureNotEmpty;
import static io.werval.util.Strings.EMPTY;
import static io.werval.util.Strings.isEmpty;

/**
 * Sanitize and encode.
 */
// TODO Allow JIT to inline NOOP or Metrics accounting
public final class Sanitize
{
    private static final Logger LOG = LoggerFactory.getLogger( Sanitize.class );
    public static final String ANTISAMY_ANYTHINGGOES = "antisamy-anythinggoes-1.4.4.xml";
    public static final String ANTISAMY_SLASHDOT = "antisamy-slashdot-1.4.4.xml";
    public static final String ANTISAMY_MYSPACE = "antisamy-myspace-1.4.4.xml";
    public static final String ANTISAMY_EBAY = "antisamy-ebay-1.4.4.xml";
    public static final String ANTISAMY_TINYMCE = "antisamy-tinymce-1.4.4.xml";
    private final ClassLoader loader;
    private final Lang lang;
    private final AntiSamy antiSamy;
    private final CssScanner cssScanner;
    private final Metrics metrics;

    /* package */ Sanitize( ClassLoader loader, Lang lang, URL policy, Metrics metrics )
        throws SanitizeException
    {
        this.loader = loader;
        this.lang = lang;
        try
        {
            this.antiSamy = new AntiSamy( Policy.getInstance( policy ) );
            ResourceBundle messages;
            try
            {
                messages = ResourceBundle.getBundle( "AntiSamy", lang.toLocale(), loader );
            }
            catch( MissingResourceException ex )
            {
                messages = ResourceBundle.getBundle( "AntiSamy", Locale.UK, loader );
            }
            this.cssScanner = new CssScanner(
                (InternalPolicy) InternalPolicy.getInstance( policy ),
                messages
            );
        }
        catch( Exception ex )
        {
            throw new SanitizeException( ex );
        }
        this.metrics = metrics;
    }

    /**
     * Create a new {@literal Sanitize} instance using a given AntySamy policy.
     *
     * @param policyResourceName Name of the classpath resource to load the policy
     *
     * @return The new {@literal Sanitize} instance using the given AntySamy policy
     *
     * @throws SanitizeException if anything goes wrong
     */
    public Sanitize withPolicy( String policyResourceName )
        throws SanitizeException
    {
        ensureNotEmpty( "Policy resource name", policyResourceName );
        return new Sanitize( loader, lang, loader.getResource( policyResourceName ), metrics );
    }

    /**
     * Silently sanitize HTML.
     * <p>
     * Use {@literal AntiSamy}.
     *
     * @param input HTML input, may be null
     *
     * @return Sanitized HTML according the the active policy, empty string if anything goes wrong
     */
    public String html( String input )
    {
        Timer.Context timer = null;
        if( metrics != null )
        {
            timer = metrics.metrics().timer( "io.werval.modules.sanitize.html" ).time();
        }
        try
        {
            if( isEmpty( input ) )
            {
                return EMPTY;
            }
            CleanResults results = antiSamy.scan( input );
            if( results.getNumberOfErrors() > 0 )
            {
                LOG.debug(
                    "HTML sanitization filtered {} errors: {}",
                    results.getNumberOfErrors(), results.getErrorMessages()
                );
            }
            return results.getCleanHTML();
        }
        catch( Exception ex )
        {
            LOG.error( "HTML sanitization error, will return empty string: {}", ex.getMessage(), ex );
            return EMPTY;
        }
        finally
        {
            if( timer != null )
            {
                timer.close();
            }
        }
    }

    /**
     * Silently sanitize CSS.
     * <p>
     * Use {@literal AntiSamy} CssScanner.
     *
     * @param input CSS input, may be null
     *
     * @return Sanitized CSS according the the active policy, empty string if anything goes wrong
     */
    public String css( String input )
    {
        Timer.Context timer = null;
        if( metrics != null )
        {
            timer = metrics.metrics().timer( "io.werval.modules.sanitize.css" ).time();
        }
        try
        {
            if( isEmpty( input ) )
            {
                return EMPTY;
            }
            CleanResults results = cssScanner.scanStyleSheet( input, Integer.MAX_VALUE ); // Lower this!
            if( results.getNumberOfErrors() > 0 )
            {
                LOG.debug(
                    "CSS sanitization filtered {} errors: {}",
                    results.getNumberOfErrors(), results.getErrorMessages()
                );
            }
            return results.getCleanHTML();
        }
        catch( Exception ex )
        {
            LOG.error( "CSS sanitization error, will return empty string: {}", ex.getMessage(), ex );
            return EMPTY;
        }
        finally
        {
            if( timer != null )
            {
                timer.close();
            }
        }
    }

    /**
     * Silently sanitize JSON.
     * <p>
     * This can be attached at either end of a data-pipeline to help satisfy Postel's principle:
     * <blockquote>
     * be conservative in what you do, be liberal in what you accept from others
     * </blockquote>
     * <p>
     * Applied to JSON-ish content from others, it will produce well-formed JSON that should satisfy any parser you use.
     * <p>
     * Applied to your output before you send, it will coerce minor mistakes in encoding and make it easier to embed
     * your JSON in HTML and XML.
     *
     * @param input JSON input, may be null
     *
     * @return Sanitized JSON, empty string if anything goes wrong
     */
    public String json( String input )
    {
        Timer.Context timer = null;
        if( metrics != null )
        {
            timer = metrics.metrics().timer( "io.werval.modules.sanitize.json" ).time();
        }
        try
        {
            if( isEmpty( input ) )
            {
                return EMPTY;
            }
            return JsonSanitizer.sanitize( input );
        }
        catch( Exception ex )
        {
            LOG.error( "JSON sanitization error, will return empty string: {}", ex.getMessage(), ex );
            return EMPTY;
        }
        finally
        {
            if( timer != null )
            {
                timer.close();
            }
        }
    }

    /**
     * Encodes for HTML text content and text attributes.
     *
     * @param input HTML input, may be null
     *
     * @return Encoded HTML text, empty string if anything goes wrong
     */
    public String forHtml( String input )
    {
        if( isEmpty( input ) )
        {
            return EMPTY;
        }
        try
        {
            return Encode.forHtml( input );
        }
        catch( Exception ex )
        {
            LOG.error( "Encoding for HTML error, will return empty string: {}", ex.getMessage(), ex );
            return EMPTY;
        }
    }

    /**
     * Encodes for CSS strings.
     *
     * @param input CSS input, may be null
     *
     * @return Encoded CSS, empty string if anything goes wrong
     */
    public String forCssString( String input )
    {
        if( isEmpty( input ) )
        {
            return EMPTY;
        }
        try
        {
            return Encode.forCssString( input );
        }
        catch( Exception ex )
        {
            LOG.error( "Encoding for CSS string error, will return empty string: {}", ex.getMessage(), ex );
            return EMPTY;
        }
    }

    /**
     * Encodes for CSS URLs.
     *
     * @param input CSS input, may be null
     *
     * @return Encoded CSS, empty string if anything goes wrong
     */
    public String forCssUrl( String input )
    {
        if( isEmpty( input ) )
        {
            return EMPTY;
        }
        try
        {
            return Encode.forCssUrl( input );
        }
        catch( Exception ex )
        {
            LOG.error( "Encoding for CSS URL error, will return empty string: {}", ex.getMessage(), ex );
            return EMPTY;
        }
    }

    /**
     * Encodes for a JavaScript string.
     * <p>
     * Safe for use in HTML script attributes (such as onclick), script blocks, JSON files, and JavaScript source.
     *
     * @param input Javascript input, may be null
     *
     * @return Encoded Javascript, empty string if anything goes wrong
     */
    public String forJavascript( String input )
    {
        if( isEmpty( input ) )
        {
            return EMPTY;
        }
        try
        {
            return Encode.forJavaScript( input );
        }
        catch( Exception ex )
        {
            LOG.error( "Encoding for Javascript error, will return empty string: {}", ex.getMessage(), ex );
            return EMPTY;
        }
    }

    /**
     * Encodes for XML text content and text attributes.
     *
     * @param input XML input, may be null
     *
     * @return Encoded XML text, empty string if anything goes wrong
     */
    public String forXml( String input )
    {
        if( isEmpty( input ) )
        {
            return EMPTY;
        }
        try
        {
            return Encode.forXml( input );
        }
        catch( Exception ex )
        {
            LOG.error( "Encoding for XML error, will return empty string: {}", ex.getMessage(), ex );
            return EMPTY;
        }
    }

    /**
     * Encodes data for an XML CDATA section.
     *
     * @param input input, may be null
     *
     * @return Encoded CDATA, empty string if anything goes wrong
     */
    public String forCDATA( String input )
    {
        if( isEmpty( input ) )
        {
            return EMPTY;
        }
        try
        {
            return Encode.forCDATA( input );
        }
        catch( Exception ex )
        {
            LOG.error( "Encoding for CDATA error, will return empty string: {}", ex.getMessage(), ex );
            return EMPTY;
        }
    }
}
