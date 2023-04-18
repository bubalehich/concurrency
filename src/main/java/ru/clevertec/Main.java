package ru.clevertec;

import ru.clevertec.client.Client;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        var client = new Client(10);
        client.doSend();
    }
}