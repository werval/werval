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
import javax.persistence.Persistence;
import javax.persistence.PersistenceUtil;

import static org.qiweb.api.exceptions.NullArgumentException.ensureNotEmpty;

public class JPA
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

    public <T> T withTransaction( boolean readOnly, Supplier<T> block )
    {
        // TODO https://github.com/playframework/playframework/blob/2.2.x/framework/src/play-java-jpa/src/main/java/play/db/jpa/JPA.java#L173
        T result = block.get();
        return result;
    }

    /* package */ void passivate()
    {
        emfs.values().forEach( emf -> emf.close() );
    }
}
