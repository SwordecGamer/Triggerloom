
package app.triggerloom.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public final class ApplicationLifecycleExecutor implements AutoCloseable {

    private final ApplicationController controller;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, "triggerloom-lifecycle");
                thread.setDaemon(false);
                return thread;
            });

    public ApplicationLifecycleExecutor(ApplicationController controller) {
        this.controller = controller;
    }

    public CompletableFuture<Void> startAsync() {
        return submit(controller::start);
    }

    public CompletableFuture<Void> stopAsync() {
        return submit(controller::stop);
    }

    private CompletableFuture<Void> submit(LifecycleOperation operation) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            executor.execute(() -> {
                try {
                    operation.execute();
                    future.complete(null);
                } catch (Exception exception) {
                    future.completeExceptionally(exception);
                }
            });
        } catch (RejectedExecutionException exception) {
            future.completeExceptionally(exception);
        }

        return future;
    }

    @Override
    public void close() {
        executor.shutdown();
    }

    @FunctionalInterface
    private interface LifecycleOperation {
        void execute() throws Exception;
    }
}
