package org.jingineering.collectors;

import java.util.concurrent.Executor;

public class Dispatcher<R> {
    private final Executor executor;

    Dispatcher(Executor executor) {
        this.executor = executor;
    }
}
