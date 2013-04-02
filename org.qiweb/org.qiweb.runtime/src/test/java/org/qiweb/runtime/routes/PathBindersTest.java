package org.qiweb.runtime.routes;

import com.acme.app.CustomParam;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.Test;
import org.qiweb.api.routes.PathBinder;
import org.qiweb.api.routes.PathBinders;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class PathBindersTest
{

    @Test
    public void basicTest()
    {
        PathBinder<Integer> binder = new PathBinder.Integer();
        assertThat( binder.accept( String.class ), is( false ) );
        assertThat( binder.accept( Number.class ), is( false ) );
        assertThat( binder.accept( Integer.class ), is( true ) );
    }

    @Test
    public void testPathBinders()
    {
        List<PathBinder<?>> list = new ArrayList<>();

        list.add( new PathBinder.String() );
        list.add( new PathBinder.Boolean() );
        list.add( new PathBinder.Short() );
        list.add( new PathBinder.Integer() );
        list.add( new PathBinder.Long() );
        list.add( new PathBinder.Double() );
        list.add( new PathBinder.Float() );
        list.add( new PathBinder.BigInteger() );
        list.add( new PathBinder.BigDecimal() );
        list.add( new PathBinder.UUID() );

        PathBinders binders = new PathBindersInstance( list );

        // String
        assertThat( binders.bind( String.class, "name", "foo" ), equalTo( "foo" ) );
        assertThat( binders.unbind( String.class, "name", "foo" ), equalTo( "foo" ) );
        // Boolean
        assertThat( binders.bind( Boolean.class, "name", "true" ), is( true ) );
        assertThat( binders.unbind( Boolean.class, "name", true ), equalTo( "true" ) );
        // Short
        assertThat( binders.bind( Short.class, "name", "23" ), equalTo( (short) 23 ) );
        assertThat( binders.unbind( Short.class, "name", (short) 42 ), equalTo( "42" ) );
        // Integer
        assertThat( binders.bind( Integer.class, "name", "23" ), equalTo( 23 ) );
        assertThat( binders.unbind( Integer.class, "name", 42 ), equalTo( "42" ) );
        // Long
        assertThat( binders.bind( Long.class, "name", "23" ), equalTo( 23L ) );
        assertThat( binders.unbind( Long.class, "name", 42L ), equalTo( "42" ) );
        // Double
        assertThat( binders.bind( Double.class, "name", "23.42" ), equalTo( 23.42D ) );
        assertThat( binders.unbind( Double.class, "name", 42.23D ), equalTo( "42.23" ) );
        // Float
        assertThat( binders.bind( Float.class, "name", "23.42" ), equalTo( 23.42F ) );
        assertThat( binders.unbind( Float.class, "name", 42.23F ), equalTo( "42.23" ) );
        // BigInteger
        assertThat( binders.bind( BigInteger.class, "name", "23" ), equalTo( new BigInteger( "23" ) ) );
        assertThat( binders.unbind( BigInteger.class, "name", new BigInteger( "42" ) ), equalTo( "42" ) );
        // BigDecimal
        assertThat( binders.bind( BigDecimal.class, "name", "23.42" ), equalTo( new BigDecimal( "23.42" ) ) );
        assertThat( binders.unbind( BigDecimal.class, "name", new BigDecimal( "42.23" ) ), equalTo( "42.23" ) );
        // UUID
        assertThat( binders.bind( UUID.class, "name", "E461082E-B97B-4478-BFB5-DA6C79AFD3F8" ),
                    equalTo( UUID.fromString( "E461082E-B97B-4478-BFB5-DA6C79AFD3F8" ) ) );
        assertThat( binders.unbind( UUID.class, "name", UUID.fromString( "E461082E-B97B-4478-BFB5-DA6C79AFD3F8" ) ),
                    equalTo( "E461082E-B97B-4478-BFB5-DA6C79AFD3F8".toLowerCase( Locale.US ) ) );
    }

    @Test
    public void testCustom()
    {
        PathBinders binders = new PathBindersInstance( Collections.<PathBinder<?>>singletonList( new CustomParam.PathBinder() ) );
        assertThat( binders.bind( CustomParam.class, "name", "1234" ), equalTo( new CustomParam( "1234" ) ) );
        assertThat( binders.unbind( CustomParam.class, "name", new CustomParam( "1234" ) ), equalTo( "1234" ) );
    }
}
