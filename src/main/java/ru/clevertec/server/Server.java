package ru.clevertec.server;

import ru.clevertec.data.Request;
import ru.clevertec.data.Response;
import ru.clevertec.logger.ConsoleLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Server {
    private static final int MIN_TIME_SLEEP_IN_MILLIS = 100;

    private static final int MAX_TIME_SLEEP_IN_MILLIS = 1000;

    private final List<Integer> resources;

    private final Lock lock;

    public Server() {
        resources = Collections.synchronizedList(new ArrayList<>());
        lock = new ReentrantLock();
    }

    public Response process(Request request) {
        ConsoleLogger.log(String.format("SERVER: Received message: %d", request.message()));
        lock.lock();
        var message = -1;
        try {
            doSleep();
            resources.add(request.message());
            message = resources.size();
            ConsoleLogger.log(String.format("SERVER: Processing message: %d", request.message()));
        } catch (Exception e) {
            ConsoleLogger.log(String.format("SERVER: Error while receiving request %d", request.message()));
            Thread.currentThread().interrupt();
        }
        lock.unlock();

        return new Response(message);
    }

    private void doSleep() throws InterruptedException {
        var timeToSleep = getRandomInt(MIN_TIME_SLEEP_IN_MILLIS, MAX_TIME_SLEEP_IN_MILLIS);
        Thread.sleep(timeToSleep);
    }

    private int getRandomInt(int min, int max) {
        max -= min;
        return (int) (Math.random() * ++max) + min;
    }
}
