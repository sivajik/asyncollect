package org.jingineering.collectors;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * Main class to house all (static) factory methods (a facade) to many parallel {@link java.util.stream.Collector}s
 *
 * @author Sivaji Kondapalli
 */
public final class AsynCollectors {
    private AsynCollectors() {
    }

    /**
     * A {@link Collector} powered to perform tasks in parallel using java virtual threads and returning them as a
     * {@link CompletableFuture} containing a result of the application of the user-provided {@link Collector}.
     * This method takes a function argument which applied on (type) T and gives back (type) R, then collects all R's to
     * another mutable collector of type RR. The result (the collector) is wrapped in a CompletableFuture so that non-blocking,
     * composable operations can be chained.
     *
     * <br>
     * Sample:
     * <pre>
     *     {@code
     *        CompletableFuture<List<String>> result = Stream.of(130781, 110880, 310886, 260314, 291086)
     *          .collect(parallel(BirthDayUtils::printAsEnglishText, toList()));
     *     }
     * </pre>
     *
     * @param mapper    a mapper function to be performed in parallel.
     * @param collector the {@code Collector}
     * @param <T>       the type of input elements to the reduction operation
     * @param <R>       the result type of the reduction operation given by {@code mapper}
     * @param <RR>      the result type of the result captured in {@code CompletableFuture}
     * @return a {@code Collector} which collects all processed elements into a user-provided mutable {@code Collection} in parallel
     * @since 1.0.0
     */
    public static <T, R, RR> Collector<T, ?, CompletableFuture<RR>> parallel(Function<? super T, ? extends R> mapper,
                                                                             Collector<R, ?, RR> collector) {
        return AsynCollectorsFactory.collecting(stream -> stream.collect(collector), mapper);
    }

    /**
     * A {@link Collector} powered to perform tasks in parallel using java virtual threads and returning them as a
     * {@link CompletableFuture} containing a result of the application of the user-provided {@link Collector} with user
     * given {@link java.util.concurrent.Executor}.
     *
     * <br>
     * Sample:
     * <pre>
     *     {@code
     *        CompletableFuture<List<String>> result = Stream.of(130781, 110880, 310886, 260314, 291086)
     *          .collect(parallel(BirthDayUtils::printAsEnglishText, toList(), Executors.newFixedThreadPool(10)));
     *     }
     * </pre>
     *
     * @param mapper    a mapper function to be performed in parallel.
     * @param collector the {@code Collector}
     * @param <T>       the type of input elements to the reduction operation
     * @param <R>       the result type of the reduction operation given by {@code mapper}
     * @param <RR>      the result type of the result captured in {@code CompletableFuture}
     * @return a {@code Collector} which collects all processed elements into a user-provided mutable {@code Collection} in parallel
     * @since 1.0.0
     */
    public static <T, R, RR> Collector<T, ?, CompletableFuture<RR>> parallel(Function<? super T, ? extends R> mapper,
                                                                             Collector<R, ?, RR> collector,
                                                                             Executor executor) {
        return AsynCollectorsFactory.collecting(stream -> stream.collect(collector), mapper, executor);
    }
}
