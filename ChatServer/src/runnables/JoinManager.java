package runnables;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

import networking.Client;

public class JoinManager implements Runnable{         
    private volatile boolean ended;
    private volatile HashMap<String, Client> clients;       

    public synchronized HashMap<String,Client> getClients() {
        return clients;
    }


    private volatile static JoinManager instance = null;
        public static synchronized JoinManager getClientManager() {
            if (instance == null) instance = new JoinManager();
        return instance;
    }
    
    private JoinManager() {
        clients = new HashMap<String,Client>();
        ended = false;
    }

    private ServerSocket server;
    public void setServer(ServerSocket server) {
        this.server = server;
    }

    @Override
    public void run() {
        try {
            while (!ended) {
                new Thread(new MessageManager(server.accept())).start();
            }
        } catch (IOException e) {e.printStackTrace();}
    }
    
    public synchronized void end() {
        ended = true;
    }
}
