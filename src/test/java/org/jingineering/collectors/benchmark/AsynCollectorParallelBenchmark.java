package org.jingineering.collectors.benchmark;

import org.jingineering.collectors.AsynCollectors;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AsynCollectorParallelBenchmark {
    @Benchmark
    public List<Integer> asynCollectParallelVirtualThreads() {
        CompletableFuture<List<Integer>> op = IntStream.range(0, 1000)
                .boxed()
                .collect(AsynCollectors.parallel(i -> i + 1, Collectors.toList(),
                        Executors.newVirtualThreadPerTaskExecutor()));
        return op.join();
    }

    @Benchmark
    public List<Integer> asynCollectParallelStreams() {
        return IntStream.range(0, 1000)
                .parallel()
                .map(i -> i + 1)
                .boxed()
                .collect(Collectors.toList());
    }

    private static final Path BENCHMARKS_PATH = Path.of("src/test/resources/benchmarks/");

    public static void main(String[] args) throws RunnerException {
        String className = AsynCollectorParallelBenchmark.class.getSimpleName();
        new Runner(new OptionsBuilder()
                .include(className)
                .warmupIterations(3)
                .measurementIterations(3)
                .resultFormat(ResultFormatType.JSON)
                .result(BENCHMARKS_PATH.resolve("%s.json".formatted(className)).toString())
                .forks(1)
                .build()).run();
    }
}
