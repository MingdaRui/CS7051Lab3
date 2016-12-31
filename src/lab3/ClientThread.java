package lab3;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Set;

public class ClientThread extends Thread {
//	private final static String WELCOME_MSG = "<= Welcome to the XYZ chat server.\r\n";
	private final static String NAME_PROMPT = "<= Login Name?\r\n";
	private final static String BYE_MSG = "<= BYE";	
	
	private String userName;					//	user the thread is serving
	private String roomName;					// 	chat room user's currently in
	
	private DataInputStream is;					// 	input stream from client
	private PrintStream os;						// output stream to client
	
	private BufferedReader br;					// input reader
    private PrintWriter pw;						// output writer
    
	private Socket clientSocket;				// socket to connect with client
	
	private Map<String,Room> rooms;				// list of chat rooms available
	private Set<String> users;					// list of all users

	public ClientThread(Socket clientSocket, 
							Map<String,Room> rooms,
							Set<String> users) {
		this.clientSocket = clientSocket;
		this.rooms = rooms;
		this.users = users;
	}
	
	public PrintWriter getPrintWriter() {
		return pw;
	}

	public void run() {		
		try {			
			is = new DataInputStream(clientSocket.getInputStream());
			os = new PrintStream(clientSocket.getOutputStream());		
			
			br = new BufferedReader(new InputStreamReader
										(clientSocket.getInputStream()));
		    pw = new PrintWriter(clientSocket.getOutputStream(), true);
			
			String input;			
			
			while(true) {				// loop to read user's input				
				input = br.readLine().trim();
				
				if (input.startsWith("/rooms") ) {		// if client wants list of rooms
					getRooms();
				}
				
				else if (input.startsWith("j")) {	// JOIN_CHATROOM if client wants to enter a room
					joinChatRoom(input);
				}
				
				else if (input.startsWith("l") ) {	// if client wants to leave chat room
					leaveChatRoom();
				}
				
				else if (input.startsWith("d")) {	// if client wants to quit the system
					quit();
					break;
				}
				
				else {
						if (roomName != null 				// if client is in a room and
								&& input.length() > 0 &&	// message is not empty and
								( ! input.startsWith("/"))) {	// it's not start with '/'
						postMessage(input);
					}					
				}
			}			
						
			is.close();					// close input stream
			os.close();					// close output stream
			
			pw.close();					// close output printer
			br.close();					// close input reader
			
			clientSocket.close();		// close socket
		} 
		catch (IOException e) {
		}		
	}
	
	// user post a message
	private void postMessage(String input) {
		
		
		String[] words = input.split("\\[|\\]");		// split input
		if (words.length >= 8) {						
						
			String output = "CHAT: [" + words[1] + "]\n"
					        + "CLIENT_NAME: [" + words[5] + "]\n"
							+ "MESSAGE: [" +words[7] + "]\n";
		
			Room room = rooms.get(roomName);
			room.postToAll(output);	
		}	
	}
	
	// user logs in with a name
	private void logIn(String input) {
			String output = "";
			int result = validateUserName(input);
			
			if ( result != 0 ) {				// name is not good
				if (result == 1 ) {
					output = "<= Sorry, name cannot be empty.\r\n";
				}
				else if (result == 2 ) {
					output = "<= Sorry, allowed letters are [a-zA-Z0-1.-_].\r\n";
				}
				else {
					output = "<= Sorry, name taken.\r\n";
				}
				output += NAME_PROMPT;			
			}
			else {								// name is good
				synchronized (users) {													
					users.add(input);			// add user					
				}
				userName = input;			// set userName
//				output = "<= Welcome " + userName + "!\r\n";			
			}
						
			pw.print(output);				// send message to user
			pw.flush();
		}
	
	// validate a user name, return:
	// - 1 for empty name
	// - 2 for name with illegal char
	// - 3 for duplicate name
	// - 0 for valid name
	private int validateUserName(String input) {	   
		if (input.length() == 0)	{						// empty name	   		
			return 1;
		}
		   	
		if ( ! input.matches("^[a-zA-Z0-9_.-]+$")) { 		// contains illegal char	   		
			return 2;
		}	   	
		   	
		synchronized (users){							// check existing user names
			if (users.contains(input)) {				// duplicate name	   			
		   		return 3;
		   	}
		}	   	 	
		return 0;										// name is good, return 0
	}	
	
	// send list of chat rooms to user
	private void getRooms() {
		String output = "<= Rooms:\r\n";
		for(Map.Entry<String, Room> entry : rooms.entrySet()) {	// loop thru each chat room in list
			String roomName = entry.getKey();					
			Room room = entry.getValue();
			output += "<= * " + roomName + " (" + room.getRoomSize() + ")\r\n";
		}
		output += "<= end of list.\r\n";		
		
		pw.print(output);				// send message to user
		pw.flush();
	}
	
	// place user in a chat room
	private void joinChatRoom(String input) {
		String output = "";

		if (roomName != null) {			// if user is already in a chat room
			output = "<= You're already in a chat room.\r\n";
		}
		else {											// user has logged in, but not in a chat room
			String[] words = input.split("\\[|\\]");		// split input to get room name
			if (words.length < 2) {						// if no room name 
				output = "<= Room name missing.\r\n";
				
				for(String s : words) {
					System.out.println(s + "|");
				}
				
			}
			else {										// if there is a room name
				
				if(words.length >= 4) {  // assign new user a user name
					System.out.println("user name: " + words[3]);
					logIn(words[3]);
				}
				
				String name = words[1];

				Room room;
				if (rooms.get(name) != null) {
					room = rooms.get(name);			// get the room client wants
				} else {
					room = new Room(name);
					rooms.put(name, room);	
				}
				
				room.addUser(userName, this);
	
				roomName = name;
					
				output = "JOINED_CHATROOM: [" + room.getRoomName() + "]\n"
						+ "SERVER_IP: [IP address]\n"
						+ "PORT: [port number]\n"
						+ "ROOM_REF: [" + room.getRoomRef() + "]\n"
						+ "JOIN_ID: [" + room.getJoinId().get(userName) + "]\n";
				
			}			
		}		
		
		pw.print(output);				// send message to user
		pw.flush();
	}
	
	// user leaves chat room
	private void leaveChatRoom() {
		String output = "";
		if (userName == null) {					// if user has not logged in
			output = "<= You have not logged in yet.\r\n";
			output += NAME_PROMPT;				// prompt for name
		}
		
		else if (roomName == null) {			// if user is not in a chat room
			output = "<= You're not in a chat room.\r\n";
		}
			
		else {									// user has logged in, and in a chat room
			Room room = rooms.get(roomName);

			String message = "LEFT_CHATROOM: [" + room.getRoomRef() + "]\n"
					+ "JOIN_ID: [" + room.getJoinId().get(userName) + "]\n";
			room.postToAll(message);
			room.removeUser(userName);
			roomName = null;						
		}
		
		pw.print(output);				// send message to user
		pw.flush();
	}
	
	// user exits the system
	private void quit() {		
		if (userName != null) {				// if user has logged in			
			if (roomName != null)			// if user in a chat room
				leaveChatRoom();			// check out the chat room
			
			synchronized (users) {			// remove user from list
				users.remove(userName);
			}
		}
		String output = BYE_MSG;
		
		pw.print(output);				// send message to user
		pw.flush();
	}
}

