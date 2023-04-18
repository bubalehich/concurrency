package ru.clevertec.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientTest {

    @Test
    void testClient() {
        var dataCount = 10;
        Client client = new Client(10);
        var accumSize = (1 + dataCount) * (dataCount / 2);

        client.doSend();

        assertEquals(accumSize, client.getAccumulator().get());
        assertEquals(0, client.getData().size());
    }
}
