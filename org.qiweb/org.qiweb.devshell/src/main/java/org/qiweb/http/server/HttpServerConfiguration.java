package org.qiweb.http.server;

import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;

/**
 * Qi4j ConfigurationComposite for HttpServer.
 * <p>If your HttpServer is not a Service inside a Qi4j Application use {@link HttpServerConfig} instead.</p>
 */
public interface HttpServerConfiguration
    extends ConfigurationComposite
{

    // @NotEmpty
    @UseDefaults
    Property<String> listenAddress();

    // @Range( 1, 65535 )
    @UseDefaults
    Property<Integer> listenPort();
}
