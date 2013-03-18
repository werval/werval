package org.qiweb.api.http;

import java.util.List;
import java.util.Map;

public interface Result
{

    int status();

    Map<String, List<String>> headers();
}
