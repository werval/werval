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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUtil;
import org.qiweb.api.Mode;
import org.qiweb.api.context.Context;
import org.qiweb.api.context.CurrentContext;
import org.qiweb.api.filters.Filter;
import org.qiweb.api.filters.FilterChain;
import org.qiweb.api.filters.FilterWith;
import org.qiweb.api.outcomes.Outcome;
import org.qiweb.modules.jpa.internal.MetricsSessionCustomizer;
import org.qiweb.modules.jpa.internal.Slf4jSessionLogger;
import org.qiweb.modules.metrics.Metrics;
import org.qiweb.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.qiweb.api.Mode.DEV;
import static org.qiweb.api.Mode.TEST;
import static org.qiweb.modules.jpa.JPAContext.METADATA_CONTEXT_KEY;
import static org.qiweb.util.IllegalArguments.ensureNotEmpty;

/**
 * JPA 2 Plugin API.
 */
// JPA Properties -> http://eclipse.org/eclipselink/documentation/2.4/jpa/extensions/persistenceproperties_ref.htm
public final class JPA
{
    @FilterWith( TransactionalFilter.class )
    @Target( { ElementType.METHOD, ElementType.TYPE } )
    @Retention( RetentionPolicy.RUNTIME )
    @Inherited
    @Documented
    public static @interface Transactional
    {
        String persistenceUnit() default "";

        boolean readOnly() default false;
    }

    public static class TransactionalFilter
        implements Filter<Transactional>
    {
        @Override
        public CompletableFuture<Outcome> filter( FilterChain chain, Context context, Optional<Transactional> config )
        {
            JPA jpa = context.application().plugin( JPA.class );
            Function<EntityManager, CompletableFuture<Outcome>> action = (em) -> chain.next( context );
            if( config.isPresent() )
            {
                String persistenceUnit = config.get().persistenceUnit();
                boolean readOnly = config.get().readOnly();
                if( Strings.isEmpty( persistenceUnit ) )
                {
                    return jpa.supplyWithTx( readOnly, action );
                }
                return jpa.supplyWithTx( persistenceUnit, readOnly, action );
            }
            return jpa.supplyWithTx( false, action );
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger( JPA.class );
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
    private final Metrics metrics;
    // Only used out of interaction context
    private final ThreadLocal<JPAContext> threadLocalContext = ThreadLocal.withInitial( () -> new JPAContext() );

    /* package */ JPA(
        Mode mode,
        ClassLoader loader,
        Map<String, Map<String, Object>> properties,
        String defaultPersistanceUnitName,
        Metrics metrics
    )
    {
        this.mode = mode;
        this.loader = loader;
        this.unitsProperties = properties;
        this.defaultPersistanceUnitName = defaultPersistanceUnitName;
        this.metrics = metrics;
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

    public EntityManagerFactory emf( String persistenceUnitName )
    {
        synchronized( emfs )
        {
            if( !emfs.containsKey( persistenceUnitName ) )
            {
                Map<String, Object> props = new HashMap<>();
                props.putAll( GLOBAL_UNITS_PROPERTIES );
                if( mode == DEV || mode == TEST )
                {
                    // Log query parameters in dev mode
                    props.put( "eclipselink.logging.parameters", "true" );
                }
                if( unitsProperties.containsKey( persistenceUnitName ) )
                {
                    props.putAll( unitsProperties.get( persistenceUnitName ) );
                }
                props.put( "eclipselink.classloader", loader );
                if( metrics != null )
                {
                    MetricsSessionCustomizer.metricsHack = metrics;
                    props.put( "eclipselink.session.customizer", MetricsSessionCustomizer.class.getName() );
                }
                EntityManagerFactory emf = Persistence.createEntityManagerFactory( persistenceUnitName, props );
                emfs.put( persistenceUnitName, emf );
                if( metrics != null )
                {
                    MetricsSessionCustomizer.metricsHack = null;
                }
            }
            return emfs.get( persistenceUnitName );
        }
    }

    /**
     * @return A new EntityManager, remember to close it
     */
    public EntityManager newEntityManager()
    {
        return newEntityManager( defaultPersistanceUnitName );
    }

    /**
     * @param persistenceUnitName Name of the PersistenceUnit to use
     *
     * @return A new EntityManager, remember to close it
     */
    public EntityManager newEntityManager( String persistenceUnitName )
    {
        EntityManager em = emf( persistenceUnitName ).createEntityManager();
        LOG.debug( "Created new EntityManager for the '{}' persistence unit", persistenceUnitName );
        return em;
    }

    /**
     * @return Current Context EntityManager if any, creating one if needed, stored in current Context. Otherwise, if
     *         no current Context, a new EntityManager stored in a ThreadLocal.
     */
    public EntityManager em()
    {
        return em( defaultPersistanceUnitName );
    }

    /**
     * @param persistenceUnitName Name of the PersistenceUnit to use
     *
     * @return Current Context EntityManager if any, creating one if needed, stored in current Context. Otherwise, if
     *         no current Context, a new EntityManager stored in a ThreadLocal.
     */
    public EntityManager em( String persistenceUnitName )
    {
        return CurrentContext.optional().map(
            // In context, using JPAContext from Context's MetaData
            (ctx) -> ( (JPAContext) ctx.metaData().computeIfAbsent( METADATA_CONTEXT_KEY, key -> new JPAContext() ) )
            .entityManagers()
            .computeIfAbsent(
                persistenceUnitName,
                puName -> newEntityManager( puName )
            )
        ).orElseGet(
            // Out of context, using ThreadLocal JPAContext
            () -> threadLocalContext.get().entityManagers().computeIfAbsent(
                persistenceUnitName,
                puName -> newEntityManager( puName )
            )
        );
    }

    public void runWithReadOnlyTx( Consumer<EntityManager> block )
    {
        runWithTx( defaultPersistanceUnitName, true, block );
    }

    public <T> T supplyWithReadOnlyTx( Function<EntityManager, T> block )
    {
        return supplyWithTx( defaultPersistanceUnitName, true, block );
    }

    public void runWithReadOnlyTx( String persistenceUnitName, Consumer<EntityManager> block )
    {
        runWithTx( persistenceUnitName, true, block );
    }

    public <T> T supplyWithReadOnlyTx( String persistenceUnitName, Function<EntityManager, T> block )
    {
        return supplyWithTx( persistenceUnitName, true, block );
    }

    public void runWithReadWriteTx( Consumer<EntityManager> block )
    {
        runWithTx( defaultPersistanceUnitName, false, block );
    }

    public <T> T supplyWithReadWriteTx( Function<EntityManager, T> block )
    {
        return supplyWithTx( defaultPersistanceUnitName, false, block );
    }

    public void runWithReadWriteTx( String persistenceUnitName, Consumer<EntityManager> block )
    {
        runWithTx( persistenceUnitName, false, block );
    }

    public <T> T supplyWithReadWriteTx( String persistenceUnitName, Function<EntityManager, T> block )
    {
        return supplyWithTx( persistenceUnitName, false, block );
    }

    public void runWithTx( boolean readOnly, Consumer<EntityManager> block )
    {
        runWithTx( defaultPersistanceUnitName, readOnly, block );
    }

    public <T> T supplyWithTx( boolean readOnly, Function<EntityManager, T> block )
    {
        return supplyWithTx( defaultPersistanceUnitName, readOnly, block );
    }

    public void runWithTx( String persistenceUnitName, boolean readOnly, Consumer<EntityManager> block )
    {
        supplyWithTx(
            persistenceUnitName,
            readOnly,
            (em) ->
            {
                block.accept( em );
                return null;
            }
        );
    }

    public <T> T supplyWithTx( String persistenceUnitName, boolean readOnly, Function<EntityManager, T> block )
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
    }

    /* package */ void passivate()
    {
        emfs.values().forEach( emf -> emf.close() );
    }
}
