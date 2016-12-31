package lab3;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Room {	
	private String roomName;				// room name
	static private int staticRoomRef = 0;
	private int roomRef;
	private int joinId = 0;
	private Map<String, ClientThread> roomUsers;	// key is user name, value is thread handling the user
	private Map<String, Integer> joinUserId;
	
	public Room(String roomName) {
		this.roomName = roomName;
		roomUsers = new HashMap<String, ClientThread>();
		joinUserId = new HashMap<String, Integer>();
		roomRef = staticRoomRef++;
	}
	
	// post a message to all users, except one user
	public void postToAllExceptOne(String message, String userName) {
		synchronized (roomUsers) {				
			for (Map.Entry<String, ClientThread> entry: roomUsers.entrySet()) {
				String name = entry.getKey();
				ClientThread thread = entry.getValue();
				if ( ! name.equals(userName) )
					thread.getPrintWriter().print(message);
					thread.getPrintWriter().flush();;
			}
		}
			
	}
	
	// post a message to all users in the room
	public void postToAll(String message) {
		synchronized (roomUsers) {
			Collection<ClientThread> threads =  roomUsers.values();	
			for (ClientThread thread : threads) {
				thread.getPrintWriter().print(message);
				thread.getPrintWriter().flush();
			}
		}
		
	}
	
	// add a user to chat room
	// receives:
	// 	- user name
	// 	- the thread handling the user
	public Set <String> addUser(String userName, 
								ClientThread clientThread) {
		synchronized (roomUsers) {
			roomUsers.put(userName, clientThread);
			joinId++;
			joinUserId.put(userName, joinId);
			return roomUsers.keySet();			
		}		
	}
	
	// remove a user from chat room
	// receive:
	// 	- user name
	// return:
	//	- true: if userName is in list
	//	- false: if userName is not in list
	public boolean removeUser(String userName) {
		synchronized (roomUsers) {
			if (roomUsers.containsKey(userName)) {
				roomUsers.remove(userName);
				joinUserId.remove(userName);
				return true;
			}
		}
		return false;
	}
	
	// return number of users in the room
	public int getRoomSize() {		
		synchronized (roomUsers) {
			return roomUsers.size();			
		}		
	}	
	
	public String getRoomName() {
		return roomName;
	}
	
	public int getRoomRef() {
		return roomRef;
	}

//	public void setRoomName(String roomName) {
//		this.roomName = roomName;
//	}

	public Map<String, ClientThread> getUsers() {
		return roomUsers;
	}
	
	public Map<String, Integer> getJoinId() {
		return joinUserId;
	}

//	public void setUsers(Map<String, ClientThread> users) {
//		//this.roomUsers = roomUsers;
//		this.roomUsers = users;
//	}


}
