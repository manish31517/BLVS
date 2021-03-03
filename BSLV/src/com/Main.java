package com;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import com.blockchain.Block;
import  network.*;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class Main {

    private static final String DEFAULT_SERVER_ADDR = "localhost";
    private static final int  DEFAULT_PORT= 5677;

    public static void main(String[] args) {
		System.out.println("----- MAIN MENU -----\n");
		System.out.println("1. Cast Votes");
		System.out.println("2. View Votes on Blockchain");
		System.out.println("3. Count Votes");
		System.out.println("0. Exit\n");
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter your choices");
		int ch = sc.nextInt();
		if (ch == 1)
		{
			System.out.println("\n----- Casting Votes -----\n");
			System.out.println("Please choose a role you want to be : server or client.");
			System.out.println("server PORT -  The port to listen to; \"6777\" is default port");
			System.out.println("client SERVER_ADDRESS PORT- The server address and port to connect to; \"localhost:6777\" is default add ");
			System.out.println("Make sure run the server first and then run client to connect to it");
			System.out.println(">------");
			Scanner in = new Scanner(System.in);
			String line=in.nextLine();
			String [] cmd = line.split("\\s+");
			if(cmd[0].contains("s")){
				int port = DEFAULT_PORT;

				System.out.println(cmd.length);
				if(cmd.length>1){
					try{
						port= Integer.parseInt(cmd[1]);
						System.out.println(port);
					}catch (NumberFormatException e){
						System.out.println("Error: port is not a number!");
					  in.close();
					  return;
					}
				}
				ServerManager svrMgr = new ServerManager(port);
				new Thread(svrMgr).start();
			}
			else if(cmd[0].contains("c"))
			{
				//client Selected;

				/** work as client */
				String svrAddr= DEFAULT_SERVER_ADDR;
				int svrPort = DEFAULT_PORT;
				if(cmd.length>2){
					try{
						svrAddr = cmd[1];
						svrPort = Integer.parseInt(cmd[2]);
					}catch (NumberFormatException e)
					{
						System.out.println("Error : Port is not a number");
						in.close();
						return;
					}
				}
				ClientManager clientManager = new ClientManager(svrAddr,svrPort);
			  /**----------new thread to receive message-----------*/
				new Thread(clientManager).start();
			  clientManager.startClient();
			}
			else
			{
				showHelp();
				in.close();;
				return;
			}
			in.close();
		}
		//view votes

		else if(ch==2) {
			System.out.println("\n--------------Displaying Votes---------------------\n");
			String userhomePath = System.getProperty("user.home");
			String fileName;
			fileName = userhomePath+"/Desktop/blockchain_data";
			File f = new File(fileName);

			try{
				if(!f.exists())
				{
					System.out.println("BlockChain file not found");

				}
				else
				{
					ObjectInputStream in = new ObjectInputStream((new FileInputStream(fileName)));
				    ArrayList<SealedObject> arr= (ArrayList<SealedObject>)in.readObject();
				   for(int i=1;i<arr.size();i++)
				   {
				   	System.out.println(decrypt(arr.get(i)));
				   }
				   in.close();
				}
				System.out.println("------------------------------------");
			}catch (IOException e)
			{
				e.printStackTrace();
			}catch (ClassNotFoundException e) {
				e.printStackTrace();
			}catch (NoSuchPaddingException e)
			{
				e.printStackTrace();
			}catch (NoSuchAlgorithmException e)
			{
				e.printStackTrace();
			}catch (InvalidKeyException e)
			{
				e.printStackTrace();
			}
		}


		//count votes
		else if(ch==3){
			String userhomePath = System.getProperty("user.home");
			String fileName;
			fileName = userhomePath+"/Desktop/blockchain_data";
			File f = new File(fileName);

			try{
				if(!f.exists())
				{
					System.out.println("Please cast your votes first !!!");

				}
				else
				{
					System.out.println("\n--------------Counting Votes---------------------\n");

					ObjectInputStream in = new ObjectInputStream((new FileInputStream(fileName)));
					ArrayList<SealedObject> arr= (ArrayList<SealedObject>)in.readObject();
				  HashMap<String ,Integer> voteMap = new HashMap<>();
					for(int i=1;i<arr.size();i++)
					{
						Block blk= (Block) decrypt(arr.get(i));
						String key = blk.getVoteObj().getVoterParty();
						voteMap.put(key,0);
					}
					for(int i=1;i<arr.size();i++)
					{
						Block blk= (Block) decrypt(arr.get(i));
						String key = blk.getVoteObj().getVoterParty();
						voteMap.put(key,voteMap.get(key)+1);
					}
					in.close();
					System.out.println("hello");
					for(Map.Entry<String,Integer> entry : voteMap.entrySet()){

						System.out.println(entry.getKey() + " : "+ entry.getValue());
					}
				}
				System.out.println("------------------------------------");
			}catch (IOException e)
			{
				e.printStackTrace();
			}catch (ClassNotFoundException e) {
				e.printStackTrace();
			}catch (NoSuchPaddingException e)
			{
				e.printStackTrace();
			}catch (NoSuchAlgorithmException e)
			{
				e.printStackTrace();
			}catch (InvalidKeyException e)
			{
				e.printStackTrace();
			}
		}
		else  if(ch==0)
		{
			System.exit(0);
		}
    }

   public static Object decrypt(SealedObject obj) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
	   SecretKeySpec sks = new SecretKeySpec("MyDifficutlPassw".getBytes(), "AES");
	   Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
	   cipher.init(Cipher.DECRYPT_MODE, sks);
	   try {
		   return obj.getObject(cipher);
	   } catch (ClassNotFoundException | IllegalBlockSizeException | BadPaddingException e){
	      e.printStackTrace();
	      return null;
	   }
    }

    /**-----------------show help----------------*/
    public static void showHelp(){
		System.out.println("Restart and select role as server or client");
		System.exit(0);
	}
}
