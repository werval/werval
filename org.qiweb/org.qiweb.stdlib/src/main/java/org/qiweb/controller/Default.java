package org.qiweb.controller;

import java.util.List;
import java.util.Map;
import org.qiweb.api.controllers.Controller;
import org.qiweb.api.controllers.Outcome;

/**
 * Controller providing default outcomes.
 * <p>Intended to be used in routes directly.</p>
 */
public class Default
    extends Controller
{

    /**
     * @return a 404 NOT FOUND Outcome
     */
    public Outcome notFound()
    {
        return outcomes().notFound().build();
    }

    /**
     * @return a 302 FOUND Outcome
     */
    public Outcome found( String url )
    {
        return outcomes().noContent().build();
    }

    /**
     * @return a 302 FOUND Outcome
     */
    public Outcome found( String url, Map<String, List<String>> queryString )
    {
        return outcomes().noContent().build();
    }

    /**
     * @return a 303 SEE_OTHER Outcome
     */
    public Outcome seeOther( String url )
    {
        return outcomes().seeOther( url ).build();
    }

    /**
     * @return a 303 SEE_OTHER Outcome
     */
    public Outcome seeOther( String url, Map<String, List<String>> queryString )
    {
        return outcomes().seeOther( url, queryString ).build();
    }

    /**
     * @return a 500 INTERNAL_SERVER_ERROR Outcome
     */
    public Outcome internalServerError()
    {
        return outcomes().internalServerError().build();
    }

    /**
     * @return a 501 NOT_IMPLEMENTED Outcome
     */
    public Outcome notImplemented()
    {
        return outcomes().notImplemented().build();
    }
}
