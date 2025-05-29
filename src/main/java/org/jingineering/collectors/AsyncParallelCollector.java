package org.jingineering.collectors;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class AsyncParallelCollector<T, R, C>
        implements Collector<T, List<CompletableFuture<R>>, CompletableFuture<C>> {
    private final Function<? super T, ? extends R> task;
    private final Function<Stream<R>, C> finalizer;
    private final Dispatcher<R> dispatcher;

    private AsyncParallelCollector(Function<? super T, ? extends R> task,
                                   Function<Stream<R>, C> finalizer,
                                   Dispatcher<R> dispatcher
    ) {
        this.task = task;
        this.finalizer = finalizer;
        this.dispatcher = dispatcher;
    }

    public static <T, R, C> Collector<T, ?, CompletableFuture<C>> from(Function<? super T, ? extends R> task,
                                                                       Function<Stream<R>, C> finalizer,
                                                                       Executor executor) {
        return new AsyncParallelCollector<>(task, finalizer, new Dispatcher<>(executor));
    }


    @Override
    public Supplier<List<CompletableFuture<R>>> supplier() {
        return null;
    }

    @Override
    public BiConsumer<List<CompletableFuture<R>>, T> accumulator() {
        return null;
    }

    @Override
    public BinaryOperator<List<CompletableFuture<R>>> combiner() {
        return null;
    }

    @Override
    public Function<List<CompletableFuture<R>>, CompletableFuture<C>> finisher() {
        return null;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of();
    }
}
