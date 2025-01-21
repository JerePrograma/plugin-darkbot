package lol.same.pvptest.syncer;

import lol.same.pvptest.syncer.messages.Message;

import java.io.IOException;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client {
    private final Socket socket;
    private final ConcurrentLinkedQueue<Message> received;
    private MessageStream stream;
    private volatile boolean shouldStop = false;

    public Client(Socket socket) {
        this.socket = socket;
        this.received = new ConcurrentLinkedQueue<>();
    }

    public void runLoop() {
        try {
            stream = MessageStream.init(socket);
        } catch (Exception e) {
            System.err.println("Error iniciando conexión con servidor: " + e.getMessage());
            try {
                socket.close();
            } catch (IOException e2) {
                //
            }
            return;
        }
        System.out.println("Conectado");
        while (!shouldStop) {
            try {
                var message = stream.read();
                received.add(message);
            } catch (Exception e) {
                if (!shouldStop)
                    System.err.println("Error en conexión con servidor: " + e.getMessage());
                break;
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            //
        }
        stream = null;
        System.out.println("Desconectado");
    }

    public void sendMessage(Message message) {
        if (stream == null) return;
        try {
            stream.write(message);
        } catch (IOException e) {
            System.err.println("Error enviando mensaje al servidor: " + e.getMessage());
        }
    }

    public Optional<Message> poll() {
        return Optional.ofNullable(received.poll());
    }

    public void stop() {
        shouldStop = true;
        try {
            socket.close();
        } catch (IOException e) {
            //
        }
    }
}
