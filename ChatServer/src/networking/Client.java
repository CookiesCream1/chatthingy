package networking;

import java.net.Socket;

import runnables.JoinManager;
import runnables.MessageManager;

public class Client {   //Tiny wrapper class for a client's socket, username and 

    private String dmRequest;

    private Socket socket;
    public Socket getSocket() {
        return socket;
    }
    private String username;
    public String getUsername() {
        return username;
    }
    private Client connection;      //whoever i'm chatting to, if null send to all other with null (global chat)
    public Client getConnection() {
        return connection;
    }
        
    public Client(Socket socket, Client connection, String username) {
        this.socket = socket;
        this.username = username;
        this.connection = connection;
        dmRequest = "";
    }
    public Client(Socket socket, String username) {
        this.socket = socket;
        this.username = username;
        this.connection = null;
        dmRequest = "";
    }

    public void sendMessage(String msg) { //Sends message to connection, if connection == null send to #general
        if (connection == null)
        {
            JoinManager.getClientManager().getClients().forEach((s, c) -> {   //string, client
                if (c.getConnection() == null && !s.equals(username))
                MessageManager.sendMessageTo(c,username + ": " + msg);
            });
        }
        else MessageManager.sendMessageTo(connection, username + ": " + msg);
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public boolean denyDM(String username) {        //true if there was a DM to deny
        boolean ret = JoinManager.getClientManager().getClients().get(username).dmRequest.equals(this.username);
        JoinManager.getClientManager().getClients().get(username).dmRequest = "";
        return ret;
    }
    public synchronized boolean requestDM(String username) {        //returns false if waiting, true if joining a DM (having been invited to one)

        Client other = JoinManager.getClientManager().getClients().get(username);
        dmRequest = username;
        //System.out.println(this.username + " > " + dmRequest);
        //System.out.println(other.username + " > " + other.dmRequest);
        if (other.dmRequest.equals(this.username)) {        //both wanna DM, connection established
            if (connection != null) connection.connectToGlobal();
            connectToGlobal();                      //cut previous DM
            other.connection = this;
            connection = other;
            connection.dmRequest = dmRequest = "";    //resetting DM requests
            return true;
        }
        return false;
    }

    public boolean connectToGlobal()   //returns false if already in global
    {
        
        if (connection != null) {
            //System.out.println(username + " XX " + connection.getUsername());
            connection = null;
            return true;
        }
        else return false;
    }
}
