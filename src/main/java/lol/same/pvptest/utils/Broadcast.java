package lol.same.pvptest.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Broadcast<T> implements Runnable {
    private final LinkedBlockingQueue<T> queue;
    private final List<BroadcastConsumer<T>> consumers;
    private final ConcurrentLinkedQueue<BroadcastConsumer<T>> newConsumers;
    private final ConcurrentLinkedQueue<BroadcastConsumer<T>> removedConsumers;

    private boolean shouldStop = false;

    public Broadcast() {
        this.queue = new LinkedBlockingQueue<>();
        this.consumers = new ArrayList<>();
        this.newConsumers = new ConcurrentLinkedQueue<>();
        this.removedConsumers = new ConcurrentLinkedQueue<>();
    }

    public void run() {
        while (true) {
            try {
                var obj = queue.take();
                if (shouldStop)
                    break;
                var newConsumer = newConsumers.poll();
                while (newConsumer != null) {
                    consumers.add(newConsumer);
                    newConsumer = newConsumers.poll();
                }
                var removedConsumer = removedConsumers.poll();
                while (removedConsumer != null) {
                    if (!consumers.remove(removedConsumer))
                        System.err.println("Intento de eliminar un consumidor que no existía");
                    removedConsumer = removedConsumers.poll();
                }
                for (var consumer: consumers)
                    consumer.receive(obj);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void push(T obj) {
        if (shouldStop)
            throw new RuntimeException("Broadcast detenido");
        queue.add(obj);
    }

    public BroadcastUnsubscribe subscribe(BroadcastConsumer<T> consumer) {
        newConsumers.add(consumer);
        return () -> removedConsumers.add(consumer);
    }

    public void stop() {
        shouldStop = true;
        queue.add(null);
    }
}
