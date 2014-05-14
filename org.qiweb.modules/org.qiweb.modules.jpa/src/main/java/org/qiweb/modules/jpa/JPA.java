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
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUtil;
import org.qiweb.api.Mode;
import org.qiweb.api.util.Strings;
import org.qiweb.modules.jpa.internal.Slf4jSessionLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qiweb.api.Mode.DEV;
import static org.qiweb.api.Mode.TEST;
import static org.qiweb.api.exceptions.IllegalArguments.ensureNotEmpty;

/**
 * JPA 2 Plugin API.
 */
// JPA Properties -> http://eclipse.org/eclipselink/documentation/2.4/jpa/extensions/persistenceproperties_ref.htm
public final class JPA
{
    private static final Logger LOG = LoggerFactory.getLogger( JPA.class );
    private static final String QIWEB_AUTODETECT_PU = "qiweb-autotedect-persistence-unit";
    private static final Map<String, Object> GLOBAL_UNITS_PROPERTIES;

    static
    {
        // Theses are set on all persistence units but can be overrided in application.conf
        GLOBAL_UNITS_PROPERTIES = new HashMap<>();
        GLOBAL_UNITS_PROPERTIES.put( "eclipselink.deploy-on-startup", "true" );
        GLOBAL_UNITS_PROPERTIES.put( "eclipselink.logging.logger", Slf4jSessionLogger.class.getName() );
        GLOBAL_UNITS_PROPERTIES.put( "eclipselink.logging.timestamp", "false" );
        GLOBAL_UNITS_PROPERTIES.put( "eclipselink.logging.session", "false" );
        GLOBAL_UNITS_PROPERTIES.put( "eclipselink.logging.connection", "false" );
        GLOBAL_UNITS_PROPERTIES.put( "eclipselink.logging.thread", "false" );
    }

    private final Mode mode;
    private final ClassLoader loader;
    private final Map<String, Map<String, Object>> unitsProperties;
    private final Map<String, EntityManagerFactory> emfs = new HashMap<>();
    private final String defaultPersistanceUnitName;

    /* package */ JPA(
        Mode mode,
        ClassLoader loader,
        Map<String, Map<String, Object>> properties,
        String defaultPersistanceUnitName
    )
    {
        this.mode = mode;
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
            props.putAll( GLOBAL_UNITS_PROPERTIES );
            if( mode == DEV || mode == TEST )
            {
                props.put( "eclipselink.logging.parameters", "true" );
            }
            if( unitsProperties.containsKey( persistenceUnitName ) )
            {
                props.putAll( unitsProperties.get( persistenceUnitName ) );
            }
            props.put( "eclipselink.classloader", loader );
            emfs.put(
                persistenceUnitName,
                Persistence.createEntityManagerFactory( persistenceUnitName, props )
            );
        }
        return emfs.get( persistenceUnitName );
    }

    public EntityManager em( String persistenceUnitName )
    {
        return emf( persistenceUnitName ).createEntityManager();
    }

    public void withReadOnlyTx( Consumer<EntityManager> block )
    {
        withTransaction( defaultPersistanceUnitName, true, block );
    }

    public <T> T withReadOnlyTx( Function<EntityManager, T> block )
    {
        return withTransaction( defaultPersistanceUnitName, true, block );
    }

    public void withReadOnlyTx( String persistenceUnitName, Consumer<EntityManager> block )
    {
        withTransaction( persistenceUnitName, true, block );
    }

    public <T> T withReadOnlyTx( String persistenceUnitName, Function<EntityManager, T> block )
    {
        return withTransaction( persistenceUnitName, true, block );
    }

    public void withReadWriteTx( Consumer<EntityManager> block )
    {
        withTransaction( defaultPersistanceUnitName, false, block );
    }

    public <T> T withReadWriteTx( Function<EntityManager, T> block )
    {
        return withTransaction( defaultPersistanceUnitName, false, block );
    }

    public void withReadWriteTx( String persistenceUnitName, Consumer<EntityManager> block )
    {
        withTransaction( persistenceUnitName, false, block );
    }

    public <T> T withReadWriteTx( String persistenceUnitName, Function<EntityManager, T> block )
    {
        return withTransaction( persistenceUnitName, false, block );
    }

    public void withTransaction( boolean readOnly, Consumer<EntityManager> block )
    {
        withTransaction( defaultPersistanceUnitName, readOnly, block );
    }

    public <T> T withTransaction( boolean readOnly, Function<EntityManager, T> block )
    {
        return withTransaction( defaultPersistanceUnitName, readOnly, block );
    }

    public void withTransaction( String persistenceUnitName, boolean readOnly, Consumer<EntityManager> block )
    {
        withTransaction(
            persistenceUnitName, readOnly,
            (EntityManager em) ->
            {
                block.accept( em );
                return null;
            }
        );
    }

    public <T> T withTransaction( String persistenceUnitName, boolean readOnly, Function<EntityManager, T> block )
    {
        EntityManager em = em( persistenceUnitName );
        EntityTransaction tx = null;
        if( !readOnly )
        {
            tx = em.getTransaction();
            tx.begin();
            LOG.trace( "Opened transaction with '{}' persistence unit.", persistenceUnitName );
        }
        try
        {
            T result = block.apply( em );
            if( tx != null )
            {
                if( tx.getRollbackOnly() )
                {
                    tx.rollback();
                    LOG.trace( "Rollbacked transaction with '{}' persistence unit.", persistenceUnitName );
                }
                else
                {
                    tx.commit();
                    LOG.trace( "Commited transaction with '{}' persistence unit.", persistenceUnitName );
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
                        LOG.trace(
                            "Rollbacked transaction with '{}' persistence unit, on error. "
                            + "See next error message for the root cause",
                            persistenceUnitName
                        );
                    }
                }
                catch( Exception ignored )
                {
                    LOG.warn(
                        "Failed to rollback on error while using '{}' persistence unit. {} - "
                        + "See next error message for the root cause.",
                        persistenceUnitName, ignored.getMessage(), ignored
                    );
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
