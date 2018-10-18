import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
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

		public static void main(String args[]) throws InterruptedException, IOException 
		{ 
//			TestingPlayGround server = new TestingPlayGround(5000); 
			System.out.println(1);
			System.out.println(2);
			FileWriter fw = new FileWriter(new File("test.txt"),true);
			fw.write("1");
			fw.write("2");
			fw.close();
		} 
}
