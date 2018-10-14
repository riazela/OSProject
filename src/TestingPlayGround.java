import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TestingPlayGround {
	//initialize socket and input stream 
		private Socket		 socket = null; 
		private ServerSocket server = null; 
		private BufferedReader in	 = null; 

		// constructor with port 
	    public TestingPlayGround(int port) 
		{ 
			// starts server and waits for a connection 
			try
			{ 
				server = new ServerSocket(port); 
				System.out.println("Server started"); 

				System.out.println("Waiting for a client ..."); 
				for (int i = 0; i < 2; i++) {
					socket = server.accept(); 
					new Server(socket,true);
				}
			} 
			catch(IOException i) 
			{ 
				i.printStackTrace();
			} 
		} 

		public static void main(String args[]) 
		{ 
			TestingPlayGround server = new TestingPlayGround(5000); 
		} 
}
