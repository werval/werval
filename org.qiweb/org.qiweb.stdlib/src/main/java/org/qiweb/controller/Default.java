package org.qiweb.controller;

import java.util.List;
import java.util.Map;
import org.qiweb.api.controllers.Outcome;
import org.qiweb.runtime.controllers.Outcomes;

public class Default
{

    /**
     * @return a 404 NOT FOUND Outcome
     */
    public Outcome notFound()
    {
        return Outcomes.notFound();
    }

    /**
     * @return a 302 FOUND Outcome
     */
    public Outcome found( String url )
    {
        return Outcomes.noContent();
    }

    /**
     * @return a 302 FOUND Outcome
     */
    public Outcome found( String url, Map<String, List<String>> queryString )
    {
        return Outcomes.noContent();
    }

    /**
     * @return a 303 SEE_OTHER Outcome
     */
    public Outcome seeOther( String url )
    {
        return Outcomes.seeOther( url );
    }

    /**
     * @return a 303 SEE_OTHER Outcome
     */
    public Outcome seeOther( String url, Map<String, List<String>> queryString )
    {
        return Outcomes.seeOther( url, queryString );
    }

    /**
     * @return a 500 INTERNAL_SERVER_ERROR Outcome
     */
    public Outcome internalServerError()
    {
        return Outcomes.internalServerError();
    }

    /**
     * @return a 501 NOT_IMPLEMENTED Outcome
     */
    public Outcome notImplemented()
    {
        return Outcomes.notImplemented();
    }
}
