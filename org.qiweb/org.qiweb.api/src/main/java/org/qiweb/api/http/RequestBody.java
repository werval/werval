package org.qiweb.api.http;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public interface RequestBody
{

    Map<String, List<String>> formAttributes();

    Map<String, List<Upload>> formUploads();

    InputStream asStream();

    byte[] asBytes();

    String asString();

    String asString( Charset charset );

    static interface Upload
    {

        String contentType();

        Charset charset();

        String filename();

        long length();

        InputStream asStream();

        byte[] asBytes();

        String asString();

        String asString( Charset charset );

        boolean renameTo( File destination );
    }
}
