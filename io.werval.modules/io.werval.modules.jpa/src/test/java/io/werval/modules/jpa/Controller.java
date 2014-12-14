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
package io.werval.modules.jpa;

import io.werval.api.outcomes.Outcome;
import java.util.concurrent.CompletableFuture;
import javax.persistence.EntityManager;

import static io.werval.api.context.CurrentContext.application;
import static io.werval.api.context.CurrentContext.outcomes;
import static io.werval.api.context.CurrentContext.plugin;

/**
 * Controller under test.
 */
public class Controller
{
    public Outcome directUsage()
    {
        EntityManager em = plugin( JPA.class ).em();

        em.getTransaction().begin();
        FooEntity foo = new FooEntity( "FOO" );
        em.persist( foo );
        em.getTransaction().commit();

        Long id = foo.getId();

        em.getTransaction().begin();
        foo = em.find( FooEntity.class, id );
        em.getTransaction().commit();

        return outcomes().ok( foo.toString() ).build();
    }

    public Outcome withTransaction()
    {
        JPA jpa = plugin( JPA.class );
        Long id = jpa.supplyWithReadWriteTx(
            "another",
            (em) ->
            {
                BarEntity bar = new BarEntity( "BAR" );
                em.persist( bar );
                em.flush();
                return bar.getId();
            }
        );
        String name = jpa.supplyWithReadOnlyTx(
            "another",
            (em) -> em.find( BarEntity.class, id ).getName()
        );
        return outcomes().ok( name ).build();
    }

    @JPA.Transactional
    public Outcome transactional()
    {
        EntityManager em = plugin( JPA.class ).em();
        FooEntity foo = new FooEntity( "FOO" );
        em.persist( foo );
        return outcomes().ok( foo.toString() ).build();
    }

    public CompletableFuture<Outcome> multithreadedDirectUsage()
    {
        return CompletableFuture.supplyAsync(
            () -> directUsage(),
            application().executor()
        );
    }

    public CompletableFuture<Outcome> multithreadedWithTransaction()
    {
        return CompletableFuture.supplyAsync(
            () -> withTransaction(),
            application().executor()
        );
    }

    public CompletableFuture<Outcome> multithreadedTransactional()
    {
        return CompletableFuture.supplyAsync(
            () -> transactional(),
            application().executor()
        );
    }
}
