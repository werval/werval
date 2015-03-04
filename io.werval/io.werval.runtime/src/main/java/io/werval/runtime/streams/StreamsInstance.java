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
package io.werval.runtime.streams;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.werval.api.streams.Streams;
import io.werval.util.Iterables;

import org.reactivestreams.Publisher;
import rx.Observable;

import static rx.RxReactiveStreams.toObservable;
import static rx.RxReactiveStreams.toPublisher;

/**
 * Streams Instance.
 */
public class StreamsInstance
    implements Streams
{
    @Override
    public <T> Publisher<T> publish( Iterable<T> iterable )
    {
        return toPublisher( Observable.from( iterable ) );
    }

    @Override
    public <I, O> Publisher<O> map( Publisher<I> input, Function<? super I, ? super O> function )
    {
        return toPublisher( toObservable( input ).map( i -> (O) function.apply( i ) ) );
    }

    @Override
    public <I, O> Publisher<O> flatMap( Publisher<I> input, Function<? super I, CompletableFuture<? super O>> function )
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public <T> Publisher<T> buffer( Publisher<T> publisher, int bufsize )
    {
        return toPublisher( toObservable( publisher ).buffer( bufsize ).concatMap( list -> Observable.from( list ) ) );
    }

    @Override
    public <T> Publisher<T> flatten( Publisher<Iterable<T>> publisher )
    {
        return toPublisher( toObservable( publisher ).concatMap( iterable -> Observable.from( iterable ) ) );
    }

    @Override
    public <T> Publisher<T> merge( Publisher<? extends T>... publishers )
    {
        return toPublisher( Observable.merge(
            Iterables.map( Iterables.asIterable( publishers ), p -> toObservable( p ) )
        ) );
    }

    @Override
    public <T> CompletableFuture<List<T>> toList( Publisher<T> publisher )
    {
        return fromObservable( toObservable( publisher ) );
    }

    private static <T> CompletableFuture<List<T>> fromObservable( Observable<T> observable )
    {
        final CompletableFuture<List<T>> future = new CompletableFuture<>();
        observable
            .doOnError( future::completeExceptionally )
            .toList()
            .forEach( future::complete );
        return future;
    }
}
