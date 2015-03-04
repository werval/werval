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
package io.werval.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.stream.ChunkedInput;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import static io.werval.util.Charsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Encode a Publisher&lt;ByteBuf&gt; into HTTP chunks.
 */
// TODO Handle errors!
public class ChunkedPublisher
    implements ChunkedInput<ByteBuf>
{
    final AtomicBoolean completed = new AtomicBoolean( false );
    final AtomicBoolean closed = new AtomicBoolean( false );
    final AtomicReference error = new AtomicReference<>();
    final AtomicReference<Subscription> subscription = new AtomicReference<>();
    final ArrayBlockingQueue<ByteBuf> buffer = new ArrayBlockingQueue<>( 2 );
    final AtomicLong requested = new AtomicLong();

    public ChunkedPublisher( Publisher<ByteBuf> publisher )
    {
        publisher.subscribe(
            new Subscriber<ByteBuf>()
            {
                @Override
                public void onSubscribe( Subscription sub )
                {
                    System.out.println( "==================================" );
                    System.out.println( "onSubscribe( " + sub + " )" );
                    System.out.println( "==================================" );
                    subscription.set( sub );
                }

                @Override
                public void onNext( ByteBuf bytebuf )
                {
                    System.out.println( "==================================" );
                    System.out.println( "onNext( " + bytebuf.toString( UTF_8 ) + " )" );
                    System.out.println( "==================================" );
                    requested.decrementAndGet();
                    try
                    {
                        if( !buffer.offer( bytebuf, 1L, SECONDS ) )
                        {
                            throw new RuntimeException( "Buffer contention!" );
                        }
                    }
                    catch( InterruptedException ex )
                    {
                        Thread.interrupted();
                        throw new RuntimeException( ex.getMessage(), ex );
                    }
                }

                @Override
                public void onError( Throwable throwable )
                {
                    System.out.println( "==================================" );
                    System.out.println( "onError( " + throwable.toString() + " )" );
                    throwable.printStackTrace( System.out );
                    System.out.println( "==================================" );
                    error.set( new RuntimeException( throwable.getMessage(), throwable ) );
                }

                @Override
                public void onComplete()
                {
                    System.out.println( "==================================" );
                    System.out.println( "onComplete()" );
                    System.out.println( "==================================" );
                    completed.set( true );
                }
            }
        );
    }

    @Override
    public boolean isEndOfInput()
        throws Exception
    {
        return closed.get()
               || error.get() != null
               || ( completed.get() && buffer.isEmpty() );
    }

    @Override
    public void close()
        throws Exception
    {
        closed.set( true );
        buffer.clear();
        subscription.getAndUpdate(
            sub ->
            {
                if( sub != null )
                {
                    sub.cancel();
                    return null;
                }
                return sub;
            }
        );
    }

    @Override
    public ByteBuf readChunk( ChannelHandlerContext ctx )
        throws Exception
    {
        if( isEndOfInput() )
        {
            return null;
        }
        eventuallyRequest();
        return buffer.poll();
    }

    private void eventuallyRequest()
    {
        Subscription sub = subscription.get();
        if( sub == null )
        {
            throw new IllegalStateException( "No Subscription! Race condition?" );
        }
        if( requested.get() < 2 )
        {
            sub.request( 1 );
            requested.incrementAndGet();
        }
    }
}
