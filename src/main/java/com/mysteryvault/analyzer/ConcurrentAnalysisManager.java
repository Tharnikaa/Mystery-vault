package com.mysteryvault.analyzer;

import java.util.*;
import java.util.concurrent.*;

/**
 * Orchestrates multi-threaded execution of all Analyzer tasks concurrently.
 * Owned by Member 3 (Module 8: Concurrent Analysis).
 * Thread-safe, non-blocking to Swing EDT, cleanly catches worker exceptions.
 */
public class ConcurrentAnalysisManager {

    private final ExecutorService executorService;

    public ConcurrentAnalysisManager() {
        this(Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors())));
    }

    public ConcurrentAnalysisManager(ExecutorService executorService) {
        this.executorService = Objects.requireNonNull(executorService, "executorService cannot be null");
    }

    /**
     * Executes a collection of Analyzer tasks concurrently across worker threads.
     * Captures failures gracefully so one failing task does not crash the entire analysis run.
     *
     * @param analyzers list of Analyzer implementations to run
     * @param context the immutable analysis context
     * @param timeoutSeconds maximum duration to wait for all analyzers to complete
     * @return List of completed AnalysisResult objects
     */
    public List<AnalysisResult> runAnalysisConcurrently(List<Analyzer> analyzers, AnalysisContext context, long timeoutSeconds) {
        if (analyzers == null || analyzers.isEmpty()) {
            return Collections.emptyList();
        }

        List<Future<AnalysisResult>> futures = new ArrayList<>();

        for (Analyzer analyzer : analyzers) {
            Callable<AnalysisResult> task = () -> {
                long start = System.currentTimeMillis();
                try {
                    return analyzer.analyze(context);
                } catch (Throwable t) {
                    long duration = System.currentTimeMillis() - start;
                    return AnalysisResult.failure(analyzer.getName(),
                            t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName(),
                            duration);
                }
            };
            futures.add(executorService.submit(task));
        }

        List<AnalysisResult> results = new ArrayList<>();
        long deadline = System.currentTimeMillis() + (timeoutSeconds * 1000L);

        for (int i = 0; i < futures.size(); i++) {
            Future<AnalysisResult> future = futures.get(i);
            Analyzer analyzer = analyzers.get(i);
            long remaining = Math.max(10, deadline - System.currentTimeMillis());

            try {
                AnalysisResult res = future.get(remaining, TimeUnit.MILLISECONDS);
                results.add(res);
            } catch (TimeoutException te) {
                future.cancel(true);
                results.add(AnalysisResult.failure(analyzer.getName(), "Analysis timed out after " + timeoutSeconds + "s", timeoutSeconds * 1000L));
            } catch (ExecutionException ee) {
                Throwable cause = ee.getCause() != null ? ee.getCause() : ee;
                results.add(AnalysisResult.failure(analyzer.getName(), "Execution failed: " + cause.getMessage(), 0));
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                results.add(AnalysisResult.failure(analyzer.getName(), "Analysis interrupted", 0));
            }
        }

        return Collections.unmodifiableList(results);
    }

    /**
     * Cleanly shuts down the executor pool when application exits.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
