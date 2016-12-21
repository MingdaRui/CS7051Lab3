package lab3;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Server {
	private static final int PORT_NUMBER = 1993;
	private static final String ROOM_NAME_1 = "chat";
	private static final String ROOM_NAME_2 = "hottub";
	
	private static ServerSocket serverSocket = null;		// server socket	
	private static Socket clientSocket = null;				// client socket	

	public static void main(String args[]) {
		System.out.println("Server starting...");
		Map<String,Room> rooms = new HashMap<String,Room>();// create a list of chat rooms
		Room room1 = new Room(ROOM_NAME_1);					// create a chat room named "chat"
		Room room2 = new Room(ROOM_NAME_2);					// create a chat room named "hottub"
		rooms.put(ROOM_NAME_1, room1);						// add chat rooms to list
		rooms.put(ROOM_NAME_2, room2);
		
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

