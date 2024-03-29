// A Java program for a Server 
import java.net.*; 
import java.io.*; 

public class Serversample 
{ 
	//initialize socket and input stream 
	private Socket		 socket = null; 
	private ServerSocket server = null; 
	private BufferedReader in	 = null; 

	// constructor with port 
	public Serversample(int port) 
	{ 
		// starts server and waits for a connection 
		try
		{ 
			server = new ServerSocket(port); 
			System.out.println("Server started"); 

			System.out.println("Waiting for a client ..."); 

			socket = server.accept(); 
			System.out.println("Client accepted"); 
			socket.setSoTimeout(5000);
			// takes input from the client socket 
			in = new BufferedReader(new InputStreamReader(socket.getInputStream())); 

			String line = ""; 

			// reads message from client until "Over" is sent 
			while (!line.equals("Over")) 
			{ 
				try
				{ 
					System.out.println("waiting");
					line = in.readLine();
					System.out.println("received");
					System.out.println(line); 

				} 
				catch(IOException i) 
				{ 
					System.out.println(i); 
				} 
			} 
			System.out.println("Closing connection"); 

			// close connection 
			socket.close(); 
			in.close(); 
		} 
		catch(IOException i) 
		{ 
			System.out.println(i); 
		} 
	} 

	public static void main(String args[]) 
	{ 
		Serversample server = new Serversample(5000); 
	} 
} 
