package lol.same.pvptest.syncer;

import lol.same.pvptest.syncer.messages.Disconnected;
import lol.same.pvptest.syncer.messages.Message;
import lol.same.pvptest.utils.Broadcast;

import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Broadcast<Message> messages;
    private MessageStream stream;

    private int clientPlayerId;

    public ClientHandler(Socket socket, Broadcast<Message> messages) {
        this.socket = socket;
        this.messages = messages;
    }

    public void run() {
        try {
            stream = MessageStream.init(socket);
        } catch (Exception e) {
            System.err.println("Error iniciando conexión con cliente: " + e.getMessage());
            try {
                socket.close();
            } catch (IOException e2) {
                //
            }
            return;
        }
        var remoteAddress = socket.getRemoteSocketAddress();
        System.out.println("Nueva conexión recibida de " + remoteAddress);
        var unsubscribe = messages.subscribe(this::receive);
        while (true) {
            try {
                var message = stream.read();
                if (message.fromPlayerId != 0)
                    clientPlayerId = message.fromPlayerId;
                messages.push(message);
            } catch (Exception e) {
                System.err.println("Error en conexión con cliente " + clientPlayerId + ": " + e.getMessage());
                break;
            }
        }
        unsubscribe.unsubscribe();
        try {
            socket.close();
        } catch (IOException e) {
            //
        }
        System.out.println("Cliente " + clientPlayerId + " desconectado: " + remoteAddress);
        if (clientPlayerId != 0)
            messages.push(new Disconnected(clientPlayerId));
    }

    private void receive(Message message) {
        if (stream == null) return;
        try {
            stream.write(message);
        } catch (IOException e) {
            System.err.println("Error enviando mensaje al cliente " + clientPlayerId + ": " + e.getMessage());
        }
    }
}
