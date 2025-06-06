package org.jingineering.collectors;

import java.util.ArrayList;
import java.util.Collections;
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

/**
 * Custom {@code java.util.Collector} implementation to run the mapper function in given thread executor and finish them
 * in a given {@code java.util.concurrent.CompletableFuture}. The second operation (accumulation) enqueues the task of
 * running a mapper function (usually non-blocking) to a {@code java.util.concurrent.BlockingQueue}. The final step of
 * Collector implementation (finisher) will combine the results and apply finalizer function (i.e. collecting to a
 * collector)
 *
 * @param <T>  the type of input elements to the reduction operation
 * @param <R>  the mutable accumulation type of the reduction operation
 * @param <RR> the result type of the reduction operation
 */
public class AsynParallelCollector<T, R, RR>
        implements Collector<T, List<CompletableFuture<R>>, CompletableFuture<RR>> {
    private final Function<? super T, ? extends R> mapperFunction;
    private final Function<Stream<R>, RR> finalizer;
    private final TaskDispatcher<R> taskDispatcher;

    private AsynParallelCollector(Function<? super T, ? extends R> task,
                                  Function<Stream<R>, RR> finalizer,
                                  TaskDispatcher<R> taskDispatcher
    ) {
        this.mapperFunction = task;
        this.finalizer = finalizer;
        this.taskDispatcher = taskDispatcher;
    }

    public static <T, R, RR> Collector<T, ?, CompletableFuture<RR>> from(Function<? super T, ? extends R> blockingFn,
                                                                         Function<Stream<R>, RR> finalizer,
                                                                         Executor executor) {
        return new AsynParallelCollector<>(blockingFn, finalizer, new TaskDispatcher<>(executor));
    }

    @Override
    public Supplier<List<CompletableFuture<R>>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<CompletableFuture<R>>, T> accumulator() {
        return (accumulator, element) -> {
            if (!taskDispatcher.isRunning()) {
                taskDispatcher.start();
            }
            accumulator.add(taskDispatcher.enqueue(() -> mapperFunction.apply(element)));
        };
    }

    /*
     * This is the optional step, which is executed only when the stream is processed in a parallel era, and if the
     * Stream is sequential, then this step will be skipped. The Combine step is used to combine all the elements into
     * a single container. In this method, we're supposed to return a Binary Operator function that combines two
     * accumulated containers.
     */
    @Override
    public BinaryOperator<List<CompletableFuture<R>>> combiner() {
        return (left, right) -> {
            throw new UnsupportedOperationException("this is not implemented");
        };
    }

    @Override
    public Function<List<CompletableFuture<R>>, CompletableFuture<RR>> finisher() {
        return results -> {
            taskDispatcher.stop();
            return combine(results).thenApply(finalizer);
        };
    }

    private static <R> CompletableFuture<Stream<R>> combine(List<CompletableFuture<R>> futures) {
        var combined = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(__ -> futures.stream().map(CompletableFuture::join));
        for (var future : futures) {
            future.whenComplete((result, exception) -> {
                if (exception != null) {
                    combined.completeExceptionally(exception);
                }
            });
        }
        return combined;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }
}
