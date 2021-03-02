package com;
import java.util.*;
import  network.*;
public class Main {

    private static final String DEFAULT_SERVER_ADDR = "localhost";
    private static final int  DEFAULT_PORT= 6777;

    public static void main(String[] args) {
		System.out.println("----- MAIN MENU -----\n");
		System.out.println("1. Cast Votes");
		System.out.println("2. View Votes on Blockchain");
		System.out.println("3. Count Votes");
		System.out.println("0. Cast Votes\n");
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
				ServerManager sermgr = new ServerManager(port);
			}
		}
    }
}
