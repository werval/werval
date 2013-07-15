package org.qiweb.runtime;

import java.util.ArrayList;
import org.junit.Test;
import org.qiweb.api.MetaData;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertNull;

public class MetaDataTest
{

    @Test
    public void givenMetaDataWhenUsingTypedGetterExpectCorrectResults()
    {
        MetaData meta = new MetaData();
        meta.put( "foo", "bar" );
        meta.put( "integer", new Integer( 42 ) );

        assertThat( (String) meta.get( "foo" ), equalTo( "bar" ) );
        assertThat( meta.get( String.class, "foo" ), equalTo( "bar" ) );

        assertThat( (Integer) meta.get( "integer" ), equalTo( 42 ) );
        assertThat( meta.get( Integer.class, "integer" ), equalTo( 42 ) );

        assertNull( meta.get( "bazar" ) );
        assertNull( meta.get( ArrayList.class, "bazar" ) );
    }
}
