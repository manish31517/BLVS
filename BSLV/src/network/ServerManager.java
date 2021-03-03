package network;
import javax.crypto.SealedObject;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
* Server Manager:- Responsible for all the network communication in server side
* In a new thread , it will run a loop accepting all the incoming clients
* and create new instances of SeverHandler in a new thread reading incoming
* message from client request
*
* In the main thread , it provides a message handler handling all the incoming
 * message. Also, it has interfaces serving ClusterManager
*
*
*
*
* */
public class ServerManager extends NetworkManager {
    private ServerSocket server = null;

    /**
     * manage the increasing client id to assign a new client an id
     */
    private volatile AtomicInteger cid = null;

    /**
     * maintain the map between client id and socket of  a  client
     */
    private volatile Map<Integer, Socket> clients = null;

    public ServerManager(int srvPort) {
        try {
            clients = new ConcurrentHashMap<Integer, Socket>();
            cid = new AtomicInteger(0);
            server = new ServerSocket(srvPort);
            System.out.println("Waiting for clients......");
            System.out.println("Please connect to" + InetAddress.getLocalHost() + ":" + srvPort + ".");


        } catch (IOException e) {
            System.out.println("ERROR: failed to listen on port " + srvPort);
            e.printStackTrace();

        }
    }

    /**
     * Run a loop to accept incoming clients. Once a connection is established
     * create a new instance of ServerHandler in a new thread to receive
     * incoming message by running a loop
     */

    @Override
    public void run() {
        while (true) {
            try {
                /**
                 * accepting new clients
                 * */
                Socket socket = server.accept();
                addClient(socket);
                System.out.println("New Client (cid is " + getCid(socket) + ") connected");
                /**
                 * create a new instance of ServerHandler to receive Message
                 * */
                new ServerHandler(this, socket).start();
                /**
                 * send the client id to the new client
                 * */
                sendMessage(socket, new Message(2, Integer.valueOf(getCid(socket))));
            } catch (IOException e) {
                continue;
            }
        }
    }

    public void clientDisconnected(Socket client) {
        int cid = getCid(client);
        System.out.println("Client " + cid + " has disconnected");
        deleteClien(cid);
    }

    /**
     * -------------------Message Handler begin------------------------------
     */
    @Override
    public void msgHandler(Message msg, Socket src) {
        switch (msg.code) {
            case 0:
                /* message type sent from server to client*/
                // client manage this
                break;
            case 1:
                /** message type sent from broadcast to all client */
                System.out.println("Broadcasting :" + (String) msg.content.toString());
                broadcast((SealedObject) msg.content, src);
                break;
            default:
                break;
        }
    }

    public void broadcast(SealedObject obj, Socket src) {
        // broadCast to all except src
        int srcCid = getCid(src);
        // System.out.println("Source id"+ srcCid)
        for (int i = cid.get() - 1; i >= 0; i--) {
            if (i != srcCid) {
                try {
                    sendMessage(getClient(i), new Message(0, obj));
                } catch (IOException e) {
                    System.out.println("Error: connection with" + srcCid + " is broken, message cannot be sent !");
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    continue;
                }

            }
        }

    }
    /*-------------------- Message handlers end ----------------------------*/

    /**
     * ---------------------Client info manage methods begin-----------------
     */
    private void addClient(Socket socket) {
        clients.put(Integer.valueOf(cid.getAndIncrement()), socket);
    }

    private boolean deleteClien(int indx) {
        if (clients.remove((Integer.valueOf(indx))) == null) {
            System.out.println("Delete failed!!!");
            return false;
        }
        return true;
    }

    private Socket getClient(int cid) {
        return (Socket) clients.get(Integer.valueOf(cid));
    }

    private int getCid(Socket socket) {
        for (Map.Entry<Integer, Socket> entry : clients.entrySet()) {
            if (entry.getValue() == socket) {
                return entry.getKey().intValue();
            }
        }
        return -1;
    }

    /**
     * ------------------------Client info manage methods end-----------------------
     */
    public void close() {
        System.out.println("Server is about to close. All connected client will exist");
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Bye----");
    }

}