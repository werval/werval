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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import org.qiweb.api.exceptions.QiWebException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA Context.
 *
 * That is the object the plugin adds to the Context's MetaData when an interaction Context exist,
 * as a ThreadLocal otherwise.
 */
public class JPAContext
{
    private static final Logger LOG = LoggerFactory.getLogger( JPAContext.class );
    /* package */ static final String METADATA_CONTEXT_KEY = JPAContext.class.getName();
    private final Map<String, EntityManager> ems = new HashMap<>( 5 );

    public Map<String, EntityManager> entityManagers()
    {
        return ems;
    }

    /* package */ void closeFailFast()
    {
        Set<String> puNames = ems.keySet();
        for( String persistenceUnitName : puNames )
        {
            close( ems.get( persistenceUnitName ), persistenceUnitName );
            puNames.remove( persistenceUnitName );
        }
    }

    /* package */ void closeFailSafe()
    {
        List<Exception> errors = new ArrayList<>();
        Set<String> puNames = ems.keySet();
        for( String persistenceUnitName : puNames )
        {
            try
            {
                close( ems.get( persistenceUnitName ), persistenceUnitName );
            }
            catch( Exception ex )
            {
                errors.add( ex );
            }
            finally
            {
                puNames.remove( persistenceUnitName );
            }
        }
        if( !errors.isEmpty() )
        {
            QiWebException ex = new QiWebException( "Errors occured while closing all EntityManagers" );
            errors.forEach( err -> ex.addSuppressed( err ) );
            throw ex;
        }
    }

    private void close( EntityManager em, String persistenceUnitName )
    {
        if( em.isOpen() )
        {
            try
            {
                if( em.isJoinedToTransaction() )
                {
                    EntityTransaction tx = em.getTransaction();
                    if( tx.isActive() )
                    {
                        if( !tx.getRollbackOnly() )
                        {
                            LOG.warn(
                                "Dangling transaction detected for '{}' persistence unit, it will be rollbacked! - {}",
                                persistenceUnitName, tx
                            );
                        }
                        try
                        {
                            tx.rollback();
                        }
                        catch( Exception ex )
                        {
                            LOG.error( "Error rollbacking dangling transaction: {}", ex.getMessage(), ex );
                        }
                    }
                }
            }
            finally
            {
                em.close();
            }
        }
    }
}
