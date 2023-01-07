import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Scanner;

import runnables.MessageReciever;

public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("workpls");
        Scanner sc = new Scanner(System.in);
        System.out.print("Input server IP: ");
        SocketAddress addr;
        try {
            addr = new InetSocketAddress(InetAddress.getByName(sc.nextLine()), 6969);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            sc.close();
            return;
        }
        Socket s = new Socket();


        try{
            s.connect(addr, 10000);
        } catch (SocketException e)
        {
            System.out.println("Connection refused.");
            s.close();
            sc.close();
            return;
        } catch (SocketTimeoutException e)
        {
            System.out.println("Connection timed out.");
            s.close();
            sc.close();
            return;
        }
        DataOutputStream out = new DataOutputStream(s.getOutputStream());
        MessageReciever rec = MessageReciever.getMessageReciever();
        rec.setInputStream(s.getInputStream());
        Thread recievingThread = new Thread(rec);
        recievingThread.start();

        String msg = null;
        boolean terminated = false;
        try {
            while (!terminated) {
                msg = sc.nextLine();
                if (msg.trim().isEmpty()) out.writeUTF(msg);
            }
        } catch (SocketException e) {
            System.out.println("Connection terminated.");
            terminated = true;
        }
        s.close();
        sc.close();
    }
}
