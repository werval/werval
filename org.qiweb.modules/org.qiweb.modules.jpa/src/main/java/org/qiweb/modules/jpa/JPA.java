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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUtil;

import static org.qiweb.api.exceptions.IllegalArguments.ensureNotEmpty;

/**
 * JPA 2 Plugin API.
 */
public final class JPA
{
    private final ClassLoader loader;
    private final Map<String, Map<String, ?>> unitsProperties;
    private final Map<String, EntityManagerFactory> emfs = new HashMap<>();
    private final String defaultPersistanceUnitName;

    /* package */ JPA( ClassLoader loader, Map<String, Map<String, ?>> properties, String defaultPersistanceUnitName )
    {
        this.loader = loader;
        this.unitsProperties = properties;
        this.defaultPersistanceUnitName = defaultPersistanceUnitName;
    }

    public PersistenceUtil util()
    {
        return Persistence.getPersistenceUtil();
    }

    public EntityManagerFactory emf()
    {
        ensureNotEmpty( "jpa.default_pu_name", defaultPersistanceUnitName );
        return emf( defaultPersistanceUnitName );
    }

    public EntityManager em()
    {
        return emf().createEntityManager();
    }

    public EntityManagerFactory emf( String persistenceUnitName )
    {
        if( !emfs.containsKey( persistenceUnitName ) )
        {
            Map<String, Object> props = new HashMap<>();
            if( unitsProperties.containsKey( persistenceUnitName ) )
            {
                props.putAll( unitsProperties.get( persistenceUnitName ) );
            }
            props.put( "eclipselink.classloader", loader );
            emfs.put(
                persistenceUnitName,
                Persistence.createEntityManagerFactory(
                    persistenceUnitName,
                    unitsProperties.get( persistenceUnitName )
                )
            );
        }
        return emfs.get( persistenceUnitName );
    }

    public EntityManager em( String persistenceUnitName )
    {
        return emf( persistenceUnitName ).createEntityManager();
    }

    public <T> T withReadOnlyTx( Supplier<T> block )
    {
        return withReadOnlyTx( defaultPersistanceUnitName, block );
    }

    public <T> T withReadOnlyTx( String persistenceUnitName, Supplier<T> block )
    {
        return withTransaction( persistenceUnitName, true, block );
    }

    public <T> T withReadWriteTx( Supplier<T> block )
    {
        return withReadWriteTx( defaultPersistanceUnitName, block );
    }

    public <T> T withReadWriteTx( String persistenceUnitName, Supplier<T> block )
    {
        return withTransaction( persistenceUnitName, false, block );
    }

    public <T> T withTransaction( boolean readOnly, Supplier<T> block )
    {
        return withTransaction( defaultPersistanceUnitName, readOnly, block );
    }

    public <T> T withTransaction( String persistenceUnitName, boolean readOnly, Supplier<T> block )
    {
        EntityManager em = em( persistenceUnitName );
        EntityTransaction tx = null;
        if( !readOnly )
        {
            tx = em.getTransaction();
            tx.begin();
        }
        try
        {
            T result = block.get();
            if( tx != null )
            {
                if( tx.getRollbackOnly() )
                {
                    tx.rollback();
                }
                else
                {
                    tx.commit();
                }
            }
            return result;
        }
        catch( Exception ex )
        {
            if( tx != null )
            {
                try
                {
                    if( tx.isActive() )
                    {
                        tx.rollback();
                    }
                }
                catch( Exception ignored )
                {
                }
            }
            throw ex;
        }
        finally
        {
            em.close();
        }
    }

    /* package */ void passivate()
    {
        emfs.values().forEach( emf -> emf.close() );
    }
}
