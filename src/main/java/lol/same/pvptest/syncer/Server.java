package lol.same.pvptest.syncer;

import lol.same.pvptest.syncer.messages.Message;
import lol.same.pvptest.utils.Broadcast;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    public static final int DEFAULT_PORT = 7446;
    private final Broadcast<Message> messageBroker;

    private ServerSocket socket;
    private volatile boolean shouldStop = false;

    public Server() {
        this.messageBroker = new Broadcast<>();
    }

    public void run() {
        try {
            socket = new ServerSocket(DEFAULT_PORT);
            Executors.newSingleThreadExecutor().submit(messageBroker);
            System.out.println("Servidor iniciado");
        } catch (BindException e) {
            System.out.println("Ya hay un servidor corriendo");
        } catch (IOException e) {
            System.err.println("Error iniciando servidor: " + e.getMessage());
        }
        while (!shouldStop) {
            try {
                var newClient = socket.accept();
                var handler = new ClientHandler(newClient, messageBroker);
                Executors.newSingleThreadExecutor().submit(handler);
            } catch (IOException e) {
                if (!shouldStop)
                    System.out.println("Error aceptando conexión: " + e.getMessage());
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            //
        }
        messageBroker.stop();
        System.out.println("Servidor detenido");
    }

    public void stop() {
        shouldStop = true;
        if (socket != null)
            try {
                socket.close();
            } catch (IOException e) {
                //
            }
    }
}
