package org.qiweb.http.server;

import java.io.IOException;
import java.util.Arrays;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.qi4j.api.service.importer.InstanceImporter;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.test.AbstractQi4jTest;
import org.qiweb.http.controllers.Result;
import org.qiweb.http.routes.Routes;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.qiweb.http.routes.RouteBuilder.routes;
import static org.qiweb.http.routes.RouteBuilder.parseRoute;

public class RouterTest
    extends AbstractQi4jTest
{

    private static final String LISTEN_ADDRESS = "127.0.0.1";
    private static final int LISTEN_PORT = 23023;
    private static final String BASE_URL = "http://127.0.0.1:23023/";

    public static interface FakeController
    {

        Result test();

        Result another( String id, Integer slug );

        Result index();

        Result foo();

        Result bar();
    }

    @Override
    public void assemble( ModuleAssembly ma )
        throws AssemblyException
    {
        ma.services( MemoryEntityStoreService.class );

        // Configuration
        HttpServerConfiguration http = ma.forMixin( HttpServerConfiguration.class ).declareDefaults();
        http.listenAddress().set( LISTEN_ADDRESS );
        http.listenPort().set( LISTEN_PORT );

        // Routes
        Routes routes = routes( parseRoute( "GET / " + FakeController.class.getName() + ".index()" ),
                                parseRoute( "GET /foo " + FakeController.class.getName() + ".foo()" ),
                                parseRoute( "GET /bar " + FakeController.class.getName() + ".bar()" ) );
        ma.importedServices( Routes.class ).importedBy( InstanceImporter.class ).setMetaInfo( routes );

        // HTTP Server
        ma.services( HttpServer.class ).identifiedBy( "router-test" ).instantiateOnStartup();
        ma.entities( HttpServerConfiguration.class );
    }

    @Test
    public void testRoutes()
        throws IOException
    {
        HttpClient client = new DefaultHttpClient();

        HttpResponse response = client.execute( new HttpGet( BASE_URL ) );
        soutResponse( response );
        assertThat( response.getStatusLine().getStatusCode(), equalTo( 200 ) );

        response = client.execute( new HttpPost( BASE_URL ) );
        soutResponse( response );
        assertThat( response.getStatusLine().getStatusCode(), equalTo( 404 ) );

        response = client.execute( new HttpGet( BASE_URL + "/foo" ) );
        soutResponse( response );
        assertThat( response.getStatusLine().getStatusCode(), equalTo( 200 ) );

        response = client.execute( new HttpGet( BASE_URL + "/bar" ) );
        soutResponse( response );
        assertThat( response.getStatusLine().getStatusCode(), equalTo( 200 ) );

        response = client.execute( new HttpGet( BASE_URL + "/bazar" ) );
        soutResponse( response );
        assertThat( response.getStatusLine().getStatusCode(), equalTo( 404 ) );
    }

    private void soutResponse( HttpResponse response )
        throws IOException
    {
        System.out.println( "RESPONSE STATUS:  " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase() );
        System.out.println( "RESPONSE HEADERS: " + Arrays.toString( response.getAllHeaders() ) );
        System.out.println( "RESPONSE BODY:    " + EntityUtils.toString( response.getEntity() ) );
    }
}
