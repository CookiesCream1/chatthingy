package runnables;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class MessageReciever implements Runnable {

    private boolean stopped = false;
    private static MessageReciever instance = null;

    DataInputStream input = null;
    public static MessageReciever getMessageReciever()
    {
        if (instance == null) instance = new MessageReciever();
        return instance;
    }
    public MessageReciever setInputStream(InputStream inputStream) {
        this.input = new DataInputStream(inputStream);
        return this;
    }
    @Override
    public void run() {
        try {
            while (!stopped)
            System.out.println("\n" + input.readUTF());
        } catch (EOFException e) {
        } catch (IOException e) {
        }
        System.out.println("Disconnected from server.");
    }
    public synchronized void stop()
    {
        stopped = true;
    }
}
