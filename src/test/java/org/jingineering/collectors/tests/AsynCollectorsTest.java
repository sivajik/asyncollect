package org.jingineering.collectors.tests;

import org.jingineering.collectors.AsynCollectors;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AsynCollectorsTest {
    @Test
    void testParallel() throws ExecutionException, InterruptedException {
        CompletableFuture<Set<Integer>> results = Stream
                .of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                .collect(AsynCollectors.parallel(i -> i + 10, Collectors.toSet()));
        Set<Integer> expected = Set.of(11, 12, 13, 14, 15, 16, 17, 18, 19);
        assertEquals(expected, results.get());
    }
}
