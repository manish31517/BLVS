package network;

import com.blockchain.Block;
import network.NetworkManager;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.Main.decrypt;

/**
 *
 * ClientManager
 *
 * Responsible for all the network communications at client side
 *
 * In a new thread, It will run a loop receiving message from server and
 * dispatch it to the main thread to handle.
 *
 * In the main thread, It provides a message handler handling all the incoming
 * messages. Also it has interfaces serving ProcessManager.
 *
 * */
public class ClientManager extends  NetworkManager{
  /**
   * the socket communication with server
   * */
  private Socket socket = null;
  private Block genesisBlock;
  private ArrayList<SealedObject> blockList;
  private ArrayList<String> parties;
  private HashSet<String> hashVotes;
  private int prevHash=0;
  private int clientId;
  public ClientManager(String addr, int port){
      try{
          socket = new Socket(addr,port);
          System.out.println("Connected to server: "+ addr + ":" + port);
         genesisBlock = new Block(0,"","","");
         hashVotes = new HashSet<>();
         parties = new ArrayList<>();
         parties.add("BJP");
         parties.add("INC");
         parties.add("BSP");
         parties.add("APP");

         blockList =new ArrayList<>();
         blockList.add(encrypt(genesisBlock));
      }catch (IOException e){
          System.out.println("Cannot connect to server");
          e.setStackTrace(e.getStackTrace());
          System.exit(0);
      }catch(Exception e){
           e.printStackTrace();
      }
  }
  public void startClient(){
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      System.out.println("Welcome to Voting Machine!!!");
      String choice ="y";
      do{
          Block blockObj = null;
          String voterId = null;
          String voterName = null;
          String voterParty = null;
          try{
              System.out.println("Enter Voter ID:");
              voterId = br.readLine();
              System.out.println("Enter Voter name");
              voterName = br.readLine();
              System.out.println("Vote for parties:");
              int voteChoice;
              do{
                  for(int i=0;i<parties.size();i++)
                  {
                      System.out.println((i+1)+"."+parties.get(i));
                  }
                  System.out.println("Enter your vote:");
                  voterParty = br.readLine();
                  voteChoice = Integer.parseInt(voterParty);

                  if(voteChoice > parties.size() || voteChoice <1){
                      System.out.println("Please enter correct index");
                  }
                  else
                      break;
              }while(true);
              voterParty = parties.get(voteChoice-1);
              blockObj = new Block(prevHash,voterId,voterName,voterParty);

              if(checkValidty(blockObj)) {
                  hashVotes.add(voterId);
                  sendClientMessage(new Message(1,encrypt(blockObj)));
                prevHash = blockObj.getBlockHash();
                blockList.add(encrypt(blockObj));
              }
              else
              {
                  System.out.println("Vote Invalid");
              }
              System.out.println("Cast Another Vote(y/n)?");
              choice = br.readLine();

          }catch (IOException e)
          {
              System.out.println("Error: read line failed");
              return;
          }catch (Exception e){
              e.printStackTrace(); }
      }while(choice.equals("y")||choice.equals("Y"));
      close();
  }

  /**----------Encryption--------------*/
  public  SealedObject encrypt(Block b) throws  Exception{
      SecretKeySpec sks = new SecretKeySpec("MyDifficutlPassw".getBytes(), "AES");
      // Create cipher
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      // Code to write your objet to file
      cipher.init(Cipher.ENCRYPT_MODE,sks);
      return new SealedObject(b,cipher);
  }
   /**-----------------------Message handling begins-----------------*/
    @Override
    public void msgHandler(Message msg, Socket socket) {
          switch(msg.code) {
              case 0:
                  /*
                  * message type sent from server to client
                  *
                  * */
                  try{
                      blockList.add((SealedObject)msg.content);
                      Block decryptedBlock = (Block) decrypt((SealedObject)msg.content);
                       hashVotes.add(decryptedBlock.getVoteObj().getVoterId());
                  }catch (Exception e){
                      e.printStackTrace();
                  }
                  break;
              case 1:
                  /*
                  * message type sent from broadcast to all client
                  * server manage this
                  * */
                  break;
              case 2:
                  clientId = (int)(msg.content);
                  break;
              default:
                  break;
          }
    }
    /**----------------------Message handler end -----------------------*/
/**----------------------------Decryption---------------------------------*/

    /**------------------------Send Message---------------------------*/
public void sendClientMessage(Message msg) throws  IOException{
       sendMessage(socket,msg);
}
/**------------------------Check validity of object---------------*/
private boolean checkValidty(Block blockObj){
    if(hashVotes.contains((String)blockObj.getVoteObj().getVoterId()))
        return false;
    else
        return true;
}
/**-------------Close----------------------------------------------*/

public void close(){
    String userHomePath = System.getProperty("user.home");
    String filename;
    filename=userHomePath+"/Desktop/blockchain_data";
    File f = new File(filename);
    try {
        if(!f.exists()) {
            f.createNewFile();
        }
        else
        {
            f.delete();
            f.createNewFile();
        }
        Path filePath = Paths.get(filename);
        Set<String> supportedAttr = filePath.getFileSystem().supportedFileAttributeViews();

        if (supportedAttr.contains("posix")) {
            Files.setPosixFilePermissions(filePath, PosixFilePermissions.fromString("rwxr-x---"));
        } else if (supportedAttr.contains("acl")) {
            UserPrincipal fileOwner = Files.getOwner(filePath);

            AclFileAttributeView view = Files.getFileAttributeView(filePath, AclFileAttributeView.class);

            AclEntry entry = AclEntry.newBuilder()
                    .setType(AclEntryType.ALLOW)
                    .setPrincipal(fileOwner)
                    .setPermissions(AclEntryPermission.READ_DATA,AclEntryPermission.WRITE_DATA,AclEntryPermission.EXECUTE)
                    .build();

            List<AclEntry> acl = view.getAcl();
            acl.add(0, entry);
            view.setAcl(acl);
        }
        ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(filename,true));
        o.writeObject(blockList);
        o.close();
        socket.close();

    }catch (IOException e){
        e.printStackTrace();
    }
    System.exit(0);
}


@Override
public void run() {
 while (true){
     try{
         receiveMessage(socket);
     }catch (ClassNotFoundException | IOException e){
         if(socket.isClosed()){
             System.out.println("Bye");
             System.exit(0);
         }
         System.out.println("Connection to server is broken. Please  restart client");
       close(socket);
       System.exit(0);
     }
 }
}

}