package network;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import  java.net.Socket;
/**
 * Network Manger
 *
 * Base Class of ServerManager and ClienManager to provide network operation
 * **/
public abstract class NetworkManager implements  Runnable {
    /**
     * Send a message to socket
     * */
    public void sendMessage(Socket socket, Message msg)throws IOException {
        ObjectOutputStream out;
        out= new ObjectOutputStream(socket.getOutputStream());
        out.writeObject(msg);
    }
   /**
    * Try to receive a message from socket
    *
    * */
   public void receiveMessage(Socket socket) throws  ClassNotFoundException,IOException{
       ObjectInputStream in= new ObjectInputStream(socket.getInputStream());
       Object inObj =  in.readObject();
       if(inObj instanceof  Message){
           Message msg= (Message) inObj;

       }
   }

   /**
    *
    * Close socket
    *
    * */
   public  void close(Socket socket){
       try {
           socket.close();

       }
       catch (IOException e)
       {
           e.printStackTrace();
       }
   }

   /**
    *
    * An interface for serverManager and clienManager to implement handling all
    * the incoming messages
    *
    * */

   public abstract  void msgHandler(Message msg, Socket socket);
}
