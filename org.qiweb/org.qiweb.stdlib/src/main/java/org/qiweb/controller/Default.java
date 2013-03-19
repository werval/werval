package org.qiweb.controller;

import java.util.List;
import java.util.Map;
import org.qiweb.api.http.Result;
import org.qiweb.runtime.http.controllers.Results;

public class Default
{

    /**
     * @return a 404 NOT FOUND Result
     */
    public Result notFound()
    {
        return Results.notFound();
    }

    /**
     * @return a 302 FOUND Result
     */
    public Result found( String url )
    {
        return Results.noContent();
    }

    /**
     * @return a 302 FOUND Result
     */
    public Result found( String url, Map<String, List<String>> queryString )
    {
        return Results.noContent();
    }

    /**
     * @return a 303 SEE_OTHER Result
     */
    public Result seeOther( String url )
    {
        return Results.seeOther( url );
    }

    /**
     * @return a 303 SEE_OTHER Result
     */
    public Result seeOther( String url, Map<String, List<String>> queryString )
    {
        return Results.seeOther( url, queryString );
    }

    /**
     * @return a 500 INTERNAL_SERVER_ERROR Result
     */
    public Result internalServerError()
    {
        return Results.internalServerError();
    }

    /**
     * @return a 501 NOT_IMPLEMENTED Result
     */
    public Result notImplemented()
    {
        return Results.notImplemented();
    }
}
