package lab3;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Server {
	private static final int PORT_NUMBER = 3333;
	
	private static ServerSocket serverSocket = null;		// server socket	
	private static Socket clientSocket = null;				// client socket

	public static void main(String args[]) {
		System.out.println("Server starting...");
		Map<String,Room> rooms = new HashMap<String,Room>();// create a list of chat rooms
		
		Set<String> users = new HashSet<String>();			// list of all users
		
		try {
			serverSocket = new ServerSocket(PORT_NUMBER);	// open a socket
		} 
		catch (IOException e) {
			System.out.println(e);
		}		
		
		while (true) {										// loop to wait for new clients
			try {				
				clientSocket = serverSocket.accept();		// a client is connected
				new ClientThread(clientSocket, 
									rooms,
									users).start();			// creates a thread to take over the new client				
			} 
			catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}

