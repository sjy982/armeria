/*
 * Copyright 2019 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.linecorp.armeria.common.stream;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.reactivestreams.Subscriber;

import io.netty.util.concurrent.EventExecutor;

public class TimeoutStreamMessage<T> implements StreamMessage<T> {

    private final StreamMessage<? extends T> delegate;
    private final long timeoutMillis;
    private final StreamTimeoutMode streamTimeoutMode;

    public TimeoutStreamMessage(StreamMessage<? extends T> delegate, Duration timeout, StreamTimeoutMode streamTimeoutMode) {
        this.delegate = delegate;
        this.timeoutMillis = timeout.toMillis();
        this.streamTimeoutMode = streamTimeoutMode;
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public long demand() {
        return delegate.demand();
    }

    @Override
    public CompletableFuture<Void> whenComplete() {
        return delegate.whenComplete();
    }

    @Override
    public void subscribe(Subscriber<? super T> subscriber, EventExecutor executor,
                          SubscriptionOption... options) {
        delegate.subscribe(new TimeoutSubscriber<T>(subscriber, executor, timeoutMillis, streamTimeoutMode), executor, options);
    }

    @Override
    public void abort() {
        delegate.abort();
    }

    @Override
    public void abort(Throwable cause) {
        delegate.abort(cause);
    }
}
