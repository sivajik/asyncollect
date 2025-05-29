package org.jingineering.collectors;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

public final class AsynCollectorsFactory {
    static <T, R, C> Collector<T, ?, CompletableFuture<C>> collecting(Function<Stream<R>, C> finalizer,
                                                                      Function<? super T, ? extends R> mapper) {
        return AsynParallelCollector.from(mapper, finalizer, Executors.newVirtualThreadPerTaskExecutor());
    }
}