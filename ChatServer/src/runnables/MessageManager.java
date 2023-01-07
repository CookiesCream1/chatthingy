package runnables;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

import networking.Client;

public class MessageManager implements Runnable{
    private volatile boolean ended;
    private DataInputStream input;
    private Client user;

    public MessageManager(Socket user) throws IOException{

        this.user = new Client(user, null);
        this.input = new DataInputStream(user.getInputStream());

    }
    private String username;
    @Override
        public void run() {
         try {
            sendMessageTo(user, "You're connected! Please pick a nick. No spaces, that's it.");
             do {
                 username = input.readUTF().trim();
                    if (username.isEmpty()) 

                    sendMessageTo(user, "Nameless? Edgy...No you cant come in.");

                    else if (username.contains(" "))
                    sendMessageTo(user, "Did you even read what I said? NO SPACES.");

                    else if (JoinManager.getClientManager().getClients().containsKey(username))
                    sendMessageTo(user, "No.");

                    else if (username.contains("LetMeSoloHer"))
                    sendMessageTo(user, "Heed my words. I am Malenia. Blade of Miquella. And I have never known defeat.");

                    else if (username.contains("JoshSwain"))
                    sendMessageTo(user, "There can be only one. You know that.");

                    else if (username.contains("RickAstley"))
                    sendMessageTo(user, "Never gonna let you in.");

                    else if (username.contains("MichaelRosen"))
                    sendMessageTo(user, "noice.");
                else break;
                username = null;
            } while (username == null);
            user.setUsername(username);
            JoinManager.getClientManager().getClients().put(username, user);
            broadcastMessage(username + " just joined, everyone say hi!");


            while (!ended){             //heres messages, remember the stuff please
                String msg = input.readUTF();
                if (msg.startsWith("/")) processCommand(msg);
                else {
                    user.sendMessage(msg);
                    sendMessageTo(user, ">" + username + ": " + msg);
                }
            }


            JoinManager.getClientManager().getClients().remove(username);
            user.getSocket().close();
        } catch (SocketException e) {
        } catch (EOFException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (username == null) return;
        JoinManager.getClientManager().getClients().remove(username, user);
        sendMsgToGlobal(username + " disconnected.");
    }

    private void processCommand(String cmd)
    {
        String[] args = cmd.split(" ");
        args[0] = args[0].replaceFirst("/", "");

        switch (args[0]) {
            case "help":
                sendMessageTo(user, 
                    "Commands: \n"
                +   "/help - displays this\n"
                +   "/global - re-connects you to the global chatroom, if you're DMing someone\n"
                +   "/exit - disconnects from the server\n"
                +   "/list - lists all users connected to server\n"
                +   "/dm <username> - invites to a one on one conversation with specified nickname or joins after invite from this user \n"
                +   "/msg <username> - sends one private message to a user, without joining a private conversation\n"
                );
                break;
            case "global":
                if (user.getConnection() == null)
                sendMessageTo(user, "Youve already arrived to your destination");
                else {
                    sendMessageTo(user, "Reconnected to global.");
                    sendMessageTo(user.getConnection(), user + " left the DM, reconnecting to global.");
                    user.getConnection().connectToGlobal();
                    user.connectToGlobal();
                }
                break;
            case "exit":
                sendMessageTo(user, "See ya!");
                ended = true;
                break;
            case "list":
                sendMessageTo(user, "Users online: \n"
                + String.join(", ", JoinManager.getClientManager().getClients().keySet()));
                break;
            case "dm":
                if (args.length == 1) 
                    sendMessageTo(user, "Who do you wanna DM again?");
                else if (!JoinManager.getClientManager().getClients().containsKey(args[1]))
                    sendMessageTo(user, "Username not found.");
                else if (JoinManager.getClientManager().getClients().get(args[1]).equals(user))
                    sendMessageTo(user, "What are you doing?");
                else {
                    Client usr2 = JoinManager.getClientManager().getClients().get(args[1]);

                    Client prev = user.getConnection();
                    Client prev2 = usr2.getConnection();

                    if (user.requestDM(args[1])) {
                        sendMessageTo(usr2, "DM connected: " + username + ", " + args[1]);
                        sendMessageTo(user, "DM connected: " + args[1] + ", " + username);

                        if (prev != null)
                        sendMessageTo(prev, "DM terminated, reconnecting to global.");
                        if (prev2 != null)
                        sendMessageTo(prev2, "DM terminated, reconnecting to global.");
                    }
                    else {
                        sendMessageTo(user, "DM request sent. You will be redirected once " + args[1] + " accepts.");   //!
                        sendMessageTo(usr2, username + " wants to DM! /dm " + username + " to accept and you both will be redirected.");
                    }
                }
                break;
            case "denydm":
                if (args.length == 1) 
                    sendMessageTo(user, "Who do you not wanna DM again?");
                else if (!JoinManager.getClientManager().getClients().containsKey(args[1]))
                    sendMessageTo(user, "User not found.");
                else if (!user.denyDM(args[1])) {
                    sendMessageTo(user, "No. Just no.");
                }
                else {
                    sendMessageTo(user, "Request from " + args[1] + " denied.");
                    sendMessageTo(JoinManager.getClientManager().getClients().get(args[1]), 
                    username + " denied your DM request.");
                }
                break;
            case "msg":
                if (args.length == 1) 
                    sendMessageTo(user, "Who do you wanna message again?");
                else if (!JoinManager.getClientManager().getClients().containsKey(args[1]))
                    sendMessageTo(user, "Username not found.");
                else {
                    String msg = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    sendMessageTo(JoinManager.getClientManager().getClients().get(args[1]),
                    "[" + username + " > " + args[1] + "]: " + msg);
                    sendMessageTo(user,
                    "[" + username + " > " + args[1] + "]: " + msg);
                }
                break;
            default:
                sendMessageTo(user, "Unknown command.");
        }
    }
    private static void sendMsgToGlobal(String msg) {
        JoinManager.getClientManager().getClients().forEach((s, c) -> {
            if (c.getConnection() == null)
            sendMessageTo(c, msg);

        });
    }
    public static synchronized void sendMessageTo(Client client, String msg) {
        try {
            new DataOutputStream(client.getSocket().getOutputStream()).writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void broadcastMessage(String msg) {
        JoinManager.getClientManager().getClients().forEach((s, c) -> {
            sendMessageTo(c, msg);
        });
    }
}
