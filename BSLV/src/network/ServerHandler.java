package network;

import javax.imageio.IIOException;
import java.io.IOException;
import java.net.Socket;

/**
 * ServerHandler
 *
 * SErver side class to prepare and wait for messages from  a client specified
 * by_socket
 * */

public class ServerHandler extends  Thread{
   /**
   * The socket to receive message
   * */
   private Socket socket = null;
   /**
    * used for callback
    * */
   public  ServerManager svrMgr= null;
   public  ServerHandler(ServerManager svrMgr,Socket socket){
       this.svrMgr= svrMgr;
       this.socket=socket;
   }

   /**
    * Keep running a loop to receive messages from a client specified by socket
    * once the connection is broken, call ServerManager to remove this client
    *
    * */
    @Override
    public void run() {
        while(true){
            try{
                svrMgr.receiveMessage(socket);
            }catch (IOException | ClassNotFoundException e){
                svrMgr.clientDisconnected(socket);
                break;
            }
        }
    }
}
