package lab3;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {
	
	ArrayList clientOutputStreams;
	
	public class ClientHandler implements Runnable {
		BufferedReader reader;
		Socket sock;
		
		public ClientHandler (Socket clientSocket) {
			try {
				sock = clientSocket;
				InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
				reader = new BufferedReader(isReader);
			
			} catch(Exception ex) {ex.printStackTrace();}
		} // close constructor
		
		public void run() {
			String message;
			try {
				while ((message = reader.readLine()) != null) {
					System.out.println("read " + message);
					tellEveryone(message);
					
				} // close while
			} catch(Exception ex) {ex.printStackTrace();}
		} // close run
	} // close inner class
	
	public static void main (String[] args) {
		new Server().go();
	}
	
	public void go() {
		clientOutputStreams = new ArrayList();
		try {
			ServerSocket serverSock = new ServerSocket(9312);
			
			ExecutorService pool = new ThreadPoolExecutor (0, 50, 
					60L, TimeUnit.SECONDS, 
					new SynchronousQueue<Runnable>(), 
					//new ArrayBlockingQueue<Runnable>(2),
					//new NoBlockingQueue<Runnable>(0), 
					new ThreadPoolExecutor.DiscardPolicy());
			System.out.println("Thread Pool Created, Size: 50");
			
			while(true) {
				Socket clientSocket = serverSock.accept();
				PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
				clientOutputStreams.add(writer);
				
				Thread t = new Thread(new ClientHandler(clientSocket));
				pool.execute(t);
				//t.start();
				System.out.println("got a connection");
			}
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	} // close go
	
	public void tellEveryone(String message) {
		
		Iterator it = clientOutputStreams.iterator();
		while(it.hasNext()) {
			try {
				PrintWriter writer = (PrintWriter) it.next();
				writer.println(message);
				writer.flush();
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			
		} // end while
		
	} // close tellEveryone
} // close class
