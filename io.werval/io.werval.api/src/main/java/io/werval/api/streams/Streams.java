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
package io.werval.api.streams;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import org.reactivestreams.Publisher;

/**
 * Streams.
 */
public interface Streams
{
    <T> Publisher<T> publish( Iterable<T> iterable );

    <I, O> Publisher<O> map( Publisher<I> input, Function<? super I, ? super O> function );

    <I, O> Publisher<O> flatMap( Publisher<I> input, Function<? super I, CompletableFuture<? super O>> function );

    <T> Publisher<T> buffer( Publisher<T> publisher, int bufsize );

    default <T> Publisher<T> wiretap( Publisher<T> publisher, Consumer<T> listener )
    {
        return map(
            publisher,
            item ->
            {
                listener.accept( item );
                return item;
            }
        );
    }

    <T> Publisher<T> flatten( Publisher<Iterable<T>> publisher );

    <T> Publisher<T> merge( Publisher<? extends T>... publishers );

    <T> CompletableFuture<List<T>> toList( Publisher<T> publisher );
}
