package org.qiweb.api.filters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to declare Filters on Controller types and methods.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target(
    {
    ElementType.TYPE,
    ElementType.METHOD
} )
public @interface FilterWith
{

    /**
     * @return Ordered collection of Filters
     */
    Class<? extends Filter>[] value();
}