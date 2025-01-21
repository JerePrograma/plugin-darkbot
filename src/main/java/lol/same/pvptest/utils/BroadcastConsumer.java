package lol.same.pvptest.utils;

public interface BroadcastConsumer<T> {
    void receive(T obj);
}
