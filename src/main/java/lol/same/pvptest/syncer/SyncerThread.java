package lol.same.pvptest.syncer;

import lol.same.pvptest.syncer.messages.Message;

import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SyncerThread implements Runnable {
    private volatile boolean shouldStop = false;
    private Client connectedClient = null;

    public void run() {
        while (!shouldStop) {
            // Si otra instancia ya inició un servidor este hilo se cerrara
            // pero no importa. A continuación nos conectaremos al que haya.
            var ownServer = new Server();
            Executors.newSingleThreadExecutor().submit(ownServer);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                break;
            }
            // Conectarse al servidor
            try (var socket = new Socket("127.0.0.1", Server.DEFAULT_PORT)) {
                connectedClient = new Client(socket);
                if (shouldStop)
                    throw new Exception("Hilo deteniéndose");
                connectedClient.runLoop();
            } catch (Exception e) {
                System.err.println("Error de conexión: " + e.getMessage());
            }
            connectedClient = null;
            ownServer.stop();
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                break;
            }
        }
        System.out.println("Hilo interrumpido");
    }

    public void sendMessage(Message message) {
        if (connectedClient == null) return;
        Executors.newSingleThreadExecutor()
                .submit(() -> connectedClient.sendMessage(message));
    }

    public Optional<Message> poll() {
        if (connectedClient == null) return Optional.empty();
        return connectedClient.poll();
    }

    public void stop() {
        shouldStop = true;
        if (connectedClient != null)
            Executors.newSingleThreadExecutor().submit(connectedClient::stop);
    }
}
