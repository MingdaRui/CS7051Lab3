package lab3;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
	
	BufferedReader reader;
	PrintWriter writer;
	Scanner sc;
	Socket sock;
	
	public static void main(String[] args) {
		Client client = new Client();
		client.go();
	}
	
	public void go() {
		
		setUpNetworking();
		
		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();
		
		Thread writerThread = new Thread(new OutgoingWriter());
		writerThread.start();
	
	} // close go
	
	private void setUpNetworking() {
		
		try {
			sock = new Socket("127.0.0.1", 9312);
			InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
			reader = new BufferedReader(streamReader);
			writer = new PrintWriter(sock.getOutputStream());
			sc = new Scanner(System.in);
			System.out.println("networking established");
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	} // close setUpNetworking
	
	
	public class IncomingReader implements Runnable {
		public void run() {
			String incomingMsg;
			try {
				
				while ((incomingMsg = reader.readLine()) != null) {
					System.out.println("read " + incomingMsg);
					
				} // close while
			} catch (Exception ex) {ex.printStackTrace();}
		} // close run
	} // close inner class
	
	public class OutgoingWriter implements Runnable {
		public void run() {
			String outgoingMsg;
			while(true) {
				outgoingMsg = sc.nextLine();
				writer.println(outgoingMsg);
				writer.flush();
			}
		}
	}
}
