package org.qiweb.http.controllers;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Map;

public interface Result
{

    int status();

    Map<String, List<String>> headers();

    ByteBuf body();
}
