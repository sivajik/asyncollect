package org.jingineering.collectors;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor;

public final class AsynCollectorsFactory {
    static <T, R, RR> Collector<T, ?, CompletableFuture<RR>> collecting(Function<Stream<R>, RR> finalizer,
                                                                        Function<? super T, ? extends R> mapper) {
        return AsynParallelCollector.from(mapper, finalizer, newVirtualThreadPerTaskExecutor());
    }

    static <T, R, RR> Collector<T, ?, CompletableFuture<RR>> collecting(Function<Stream<R>, RR> finalizer,
                                                                        Function<? super T, ? extends R> mapper,
                                                                        Executor executor) {
        return AsynParallelCollector.from(mapper, finalizer, executor);
    }
}