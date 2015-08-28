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
package io.werval.runtime.context;

import java.util.concurrent.ExecutorService;

import io.werval.api.context.Context;
import io.werval.api.context.CurrentContext;
import io.werval.api.context.ThreadContextHelper;
import io.werval.runtime.util.ExecutorServiceDecoratorAdapter;

/**
 * Context Executor.
 *
 * Propagates Context to running threads.
 */
// See http://jsr166-concurrency.10961.n7.nabble.com/Thread-local-contexts-and-CompletableFutures-td9583.html
public class ContextExecutor
    extends ExecutorServiceDecoratorAdapter
{
    public ContextExecutor( ExecutorService decorated )
    {
        super( decorated );
    }

    @Override
    public void execute( Runnable command )
    {
        final Context context = CurrentContext.optional().orElse( null );
        decorated.execute( () -> ThreadContextHelper.withContext( context, command ) );
    }
}
