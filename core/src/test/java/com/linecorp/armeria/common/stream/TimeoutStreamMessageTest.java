package com.linecorp.armeria.common.stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;

public class TimeoutStreamMessageTest {
    private EventExecutor executor;

    @BeforeEach
    public void setUp() {
        executor = new DefaultEventExecutor();
    }

    @AfterEach
    public void tearDown() {
        executor.shutdownGracefully();
    }


    @Test
    public void timeoutNextMode() {
        StreamMessage<String> timeoutStreamMessage = StreamMessage.of("message1", "message2").timeout(
                Duration.ofSeconds(1), StreamTimeoutMode.UNTIL_NEXT);
        CompletableFuture<Void> future = new CompletableFuture<>();

        timeoutStreamMessage.subscribe(new Subscriber<String>() {
            private Subscription subscription;
            @Override
            public void onSubscribe(Subscription s) {
                subscription = s;
                subscription.request(1);
            }

            @Override
            public void onNext(String s) {
                executor.schedule(() -> subscription.request(1), 2, TimeUnit.SECONDS);
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                future.complete(null);
            }
        }, executor);

        assertThatThrownBy(future::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(TimeoutException.class);
    }

    @Test
    public void noTimeoutNextMode() throws Exception {
        StreamMessage<String> timeoutStreamMessage = StreamMessage.of("message1", "message2").timeout(Duration.ofSeconds(1), StreamTimeoutMode.UNTIL_NEXT);

        CompletableFuture<Void> future = new CompletableFuture<>();

        timeoutStreamMessage.subscribe(new Subscriber<String>() {
            @Override
            public void onSubscribe(Subscription subscription) {
                subscription.request(2);
            }

            @Override
            public void onNext(String s) {
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                future.complete(null);
            }
        }, executor);

        assertThat(future.get()).isNull();
    }

    @Test
    public void timeoutFirstMode() {
        StreamMessage<String> timeoutStreamMessage = StreamMessage.of("message1", "message2").timeout(Duration.ofSeconds(1), StreamTimeoutMode.UNTIL_FIRST);
        CompletableFuture<Void> future = new CompletableFuture<>();

        timeoutStreamMessage.subscribe(new Subscriber<String>() {
            private Subscription subscription;
            @Override
            public void onSubscribe(Subscription s) {
                subscription = s;
                executor.schedule(() -> subscription.request(1), 2, TimeUnit.SECONDS);
            }

            @Override
            public void onNext(String s) {
                subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                future.complete(null);
            }
        }, executor);

        assertThatThrownBy(future::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(TimeoutException.class);
    }

    @Test
    public void noTimeoutModeFirst() throws Exception {
        StreamMessage<String> timeoutStreamMessage = StreamMessage.of("message1", "message2").timeout(Duration.ofSeconds(1), StreamTimeoutMode.UNTIL_FIRST);
        CompletableFuture<Void> future = new CompletableFuture<>();

        timeoutStreamMessage.subscribe(new Subscriber<String>() {
            @Override
            public void onSubscribe(Subscription subscription) {
                subscription.request(2);
            }

            @Override
            public void onNext(String s) {
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                future.complete(null);
            }
        }, executor);

        assertThat(future.get()).isNull();
    }

    @Test
    public void timeoutEOSMode() {
        StreamMessage<String> timeoutStreamMessage = StreamMessage.of("message1", "message2").timeout(Duration.ofSeconds(2), StreamTimeoutMode.UNTIL_EOS);
        CompletableFuture<Void> future = new CompletableFuture<>();

        timeoutStreamMessage.subscribe(new Subscriber<String>() {
            private Subscription subscription;
            @Override
            public void onSubscribe(Subscription s) {
                subscription = s;
                executor.schedule(() -> subscription.request(1), 1, TimeUnit.SECONDS);
            }

            @Override
            public void onNext(String s) {
                executor.schedule(() -> subscription.request(1), 2, TimeUnit.SECONDS);
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                future.complete(null);
            }
        }, executor);

        assertThatThrownBy(future::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(TimeoutException.class);
    }

    @Test
    public void noTimeoutEOSMode() throws Exception {
        StreamMessage<String> timeoutStreamMessage = StreamMessage.of("message1", "message2").timeout(Duration.ofSeconds(2), StreamTimeoutMode.UNTIL_EOS);
        CompletableFuture<Void> future = new CompletableFuture<>();

        timeoutStreamMessage.subscribe(new Subscriber<String>() {
            @Override
            public void onSubscribe(Subscription subscription) {
                subscription.request(2);
            }

            @Override
            public void onNext(String s) {
            }

            @Override
            public void onError(Throwable throwable) {
                future.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                future.complete(null);
            }
        }, executor);

        assertThat(future.get()).isNull();
    }
}