package org.jingineering.collectors;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

public class TaskDispatcher<T> {
    private final Executor executor;
    private final ThreadFactory threadFactory = Thread::startVirtualThread;
    private final BlockingQueue<Runnable> workingQueue = new LinkedBlockingQueue<>();
    private final AtomicBoolean started = new AtomicBoolean(false);
    private static final Runnable STOP = () -> System.out.println("stop");
    private final CompletableFuture<Void> completionSignaller = new CompletableFuture<>();
    private volatile boolean shortCircuited = false;

    TaskDispatcher(Executor executor) {
        this.executor = executor;
    }

    void start() {
        if (!started.getAndSet(true)) {
            threadFactory.newThread(() -> {
                try {
                    while (true) {
                        Runnable task;
                        if ((task = workingQueue.take()) != STOP) {
                            retry(() -> executor.execute(() -> {
                                try {
                                    task.run();
                                } finally {

                                }
                            }));
                        } else {
                            break;
                        }
                    }
                } catch (Throwable e) {

                }
            });
        }
    }

    void stop() {
        try {
            workingQueue.put(STOP);
        } catch (InterruptedException e) {
            completionSignaller.completeExceptionally(e);
        }
    }

    boolean isRunning() {
        return started.get();
    }

    private static void retry(Runnable runnable) {
        try {
            runnable.run();
        } catch (RejectedExecutionException e) {
            Thread.onSpinWait();
            runnable.run();
        }
    }

    public CompletableFuture<T> enqueue(Supplier<T> supplier) {
        InterruptibleCompletableFuture<T> future = new InterruptibleCompletableFuture<>();
        workingQueue.add(completionTask(supplier, future));
        completionSignaller.exceptionally(shortCircuit(future));
        return future;
    }

    private static Function<Throwable, Void> shortCircuit(InterruptibleCompletableFuture<?> future) {
        return throwable -> {
            future.completeExceptionally(throwable);
            future.cancel(true);
            return null;
        };
    }

    private FutureTask<Void> completionTask(Supplier<T> supplier, InterruptibleCompletableFuture<T> future) {
        FutureTask<Void> task = new FutureTask<>(() -> {
            try {
                if (!shortCircuited) {
                    future.complete(supplier.get());
                }
            } catch (Throwable e) {
                handle(e);
            }
        }, null);
        future.completedBy(task);
        return task;
    }

    private void handle(Throwable e) {
        shortCircuited = true;
        completionSignaller.completeExceptionally(e);
    }

    static final class InterruptibleCompletableFuture<T> extends CompletableFuture<T> {
        private volatile FutureTask<?> backingTask;

        private void completedBy(FutureTask<Void> task) {
            backingTask = task;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (backingTask != null) {
                backingTask.cancel(mayInterruptIfRunning);
            }
            return super.cancel(mayInterruptIfRunning);
        }
    }
}
