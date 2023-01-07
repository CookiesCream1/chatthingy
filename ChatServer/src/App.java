import java.net.ServerSocket;

import runnables.JoinManager;

public class App {
    private static ServerSocket server;
    public static void main(String[] args) throws Exception {

        
        server = new ServerSocket(6969);


        JoinManager.getClientManager().setServer(server);
        Thread clientMgr = new Thread(JoinManager.getClientManager());

        clientMgr.start();
    }
    public static ServerSocket getServer()
    {
        return server;
    }
}
