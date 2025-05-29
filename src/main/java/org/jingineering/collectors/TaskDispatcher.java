package org.jingineering.collectors;

import java.util.concurrent.Executor;

public class TaskDispatcher<R> {
    private final Executor executor;

    TaskDispatcher(Executor executor) {
        this.executor = executor;
    }
}
