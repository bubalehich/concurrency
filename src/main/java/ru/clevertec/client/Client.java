package ru.clevertec.client;

import ru.clevertec.data.Request;
import ru.clevertec.data.Response;
import ru.clevertec.exception.ClientException;
import ru.clevertec.logger.ConsoleLogger;
import ru.clevertec.server.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {

    private final List<Integer> data;

    private final int dataCount;

    private final ExecutorService executor;

    private final Server server;

    private final AtomicInteger accumulator;

    public Client(int dataCount) {
        this.dataCount = dataCount;
        data = new ArrayList<>();
        populateData();
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        server = new Server();
        accumulator = new AtomicInteger(0);
    }

    public List<Integer> getData() {
        return data;
    }

    public AtomicInteger getAccumulator() {
        return accumulator;
    }

    public void doSend() {
        List<Callable<Response>> tasks = new ArrayList<>();

        for (int i = 0; i < dataCount; i++) {
            Collections.shuffle(data);
            var value = data.get(0);
            data.remove(value);

            var request = new Request(value);
            Callable<Response> response = () -> server.process(request);

            tasks.add(response);
        }
        ConsoleLogger.log(String.format("Tasks have been created. Count: %d", tasks.size()));

        List<Future<Response>> futures;
        try {
            futures = executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            ConsoleLogger.log(String.format("CLIENT: Error while invoking tasks. Reason: %s", e));
            Thread.currentThread().interrupt();
            throw new ClientException(e);
        }

        futures.parallelStream().forEach(future ->
        {
            try {
                accumulator.getAndAdd(future.get().message());
                ConsoleLogger.log(String.format("CLIENT: Response received: %d", future.get().message()));
            } catch (InterruptedException | ExecutionException e) {
                ConsoleLogger.log(String.format("CLIENT: Error while receiving response: %s. Cause: %s", future, e));
                Thread.currentThread().interrupt();
                throw new ClientException(e);
            }
        });

        destroy();
    }

    private void destroy() {
        var notFinishedTasks = executor.shutdownNow();
        var shutdownMessage = !notFinishedTasks.isEmpty()
                ? String.format("Completed with not finished tasks. Count: %s.", notFinishedTasks.size())
                : "Shutdown executor service. Completed without not finished tasks.";

        ConsoleLogger.log(shutdownMessage);
    }

    private void populateData() {
        for (int i = 0; i < dataCount; i++) {
            data.add(i);
        }
    }
}
