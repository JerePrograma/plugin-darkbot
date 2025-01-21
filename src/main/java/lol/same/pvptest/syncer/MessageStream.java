package lol.same.pvptest.syncer;

import lol.same.pvptest.syncer.messages.Message;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.security.GeneralSecurityException;

public class MessageStream {
    private static final SecretKey ENCRYPTION_KEY = new SecretKeySpec(new byte[]{
            73, -111, 10, 69, -121, 117, 35, 127, -35, -83, 9, 85, 0, -48, -81, -38,
            105, 76, -119, -31, 98, -22, 40, -101, -3, -46, -75, 85, 67, -84, -67, 17},
            "AES");
    private final ObjectInputStream input;
    private final ObjectOutputStream output;

    private MessageStream(ObjectInputStream input, ObjectOutputStream output) {
        this.input = input;
        this.output = output;
    }

    public static MessageStream init(Socket socket) throws IOException, GeneralSecurityException {
        ObjectInputStream input;
        ObjectOutputStream output;
        socket.setOption(StandardSocketOptions.TCP_NODELAY, true);

        var rawOutput = socket.getOutputStream();
        var encrypt = Cipher.getInstance("AES/CTR/NoPadding");
        encrypt.init(Cipher.ENCRYPT_MODE, ENCRYPTION_KEY);
        rawOutput.write(encrypt.getIV());
        output = new ObjectOutputStream(new CipherOutputStream(rawOutput, encrypt));

        var rawInput = socket.getInputStream();
        var decrypt = Cipher.getInstance("AES/CTR/NoPadding");
        var ivInput = rawInput.readNBytes(16);
        if (ivInput.length != 16)
            throw new EOFException();
        decrypt.init(Cipher.DECRYPT_MODE, ENCRYPTION_KEY, new IvParameterSpec(ivInput));
        input = new ObjectInputStream(new CipherInputStream(rawInput, decrypt));
        return new MessageStream(input, output);
    }

    public Message read() throws IOException, ClassNotFoundException {
        synchronized (input) {
            var message = input.readObject();
            if (message instanceof Message)
                return (Message) message;
            throw new ClassNotFoundException();
        }
    }

    public void write(Message message) throws IOException {
        synchronized (output) {
            output.writeObject(message);
        }
    }
}
