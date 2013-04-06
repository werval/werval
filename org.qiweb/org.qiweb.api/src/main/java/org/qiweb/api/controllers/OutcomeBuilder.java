package org.qiweb.api.controllers;

import java.io.InputStream;

/**
 * Builder for Outcomes.
 */
public interface OutcomeBuilder
{

    OutcomeBuilder withHeader( String name, String value );

    OutcomeBuilder as( String contentType );

    OutcomeBuilder withBody( String body );

    OutcomeBuilder withBody( InputStream body );

    OutcomeBuilder withBody( InputStream body, long length );

    Outcome build();
}
