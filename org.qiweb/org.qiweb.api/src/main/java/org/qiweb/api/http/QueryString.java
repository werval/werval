package org.qiweb.api.http;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface QueryString
{

    Set<String> keys();

    String valueOf( String key );

    List<String> valuesOf( String key );

    Map<String, String> asMap();

    Map<String, List<String>> asMapAll();
}
