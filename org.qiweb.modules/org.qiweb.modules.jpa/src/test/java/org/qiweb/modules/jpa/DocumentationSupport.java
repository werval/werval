/*
 * Copyright (c) 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qiweb.modules.jpa;

import io.werval.api.Application;
import io.werval.api.outcomes.Outcome;
import javax.persistence.*;

import static io.werval.api.context.CurrentContext.*;

public class DocumentationSupport
{
    public class SomeController
    {
        public Outcome someAction()
        {
            String result = null;
            EntityManager em = plugin( JPA.class ).em();
            // Use the EntityManager to do whatever you need to
            return outcomes().ok( result ).build();
        }
    }

    public static void newEntityManager( Application application )
    {
        EntityManager em = application.plugin( JPA.class ).newEntityManager();
        try
        {
            // Use the EntityManager to do whatever you need to
        }
        finally
        {
            em.close();
        }
    }

    public class AnotherController
    {
        public Outcome someAction()
        {
            String result = null;
            EntityManager em = plugin( JPA.class ).em( "another" );
            // Use the EntityManager to do whatever you need to
            return outcomes().ok( result ).build();
        }
    }

    public static void anotherNewEntityManager( Application application )
    {
        EntityManager em = application.plugin( JPA.class ).newEntityManager( "another" );
        try
        {
            // Use the EntityManager to do whatever you need to
        }
        finally
        {
            em.close();
        }
    }
}
