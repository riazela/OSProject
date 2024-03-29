// A Java program for a Client 
import java.net.*;
import java.util.Scanner;
import java.io.*; 

public class ClientSample 
{ 
	// initialize socket and input output streams 
	private Socket socket		 = null; 
	private Scanner input = null; 
	private OutputStreamWriter out	 = null; 

	// constructor to put ip address and port 
	public ClientSample(String address, int port) 
	{ 
		// establish a connection 
		try
		{ 
			socket = new Socket(address, port); 
			System.out.println("Connected"); 

			// takes input from terminal 
			input = new Scanner(System.in); 

			// sends output to the socket 
			out = new OutputStreamWriter(socket.getOutputStream()); 
		} 
		catch(UnknownHostException u) 
		{ 
			System.out.println(u); 
		} 
		catch(IOException i) 
		{ 
			System.out.println(i); 
		} 

		// string to read message from input 
		String line = ""; 

		// keep reading until "Over" is input 
		while (!line.equals("Over")) 
		{ 
			try
			{ 
				line = input.nextLine();
				out.write(line+"\n"); 
				out.flush();
				System.out.println("sent");
			} 
			catch(IOException i) 
			{ 
				System.out.println(i); 
			} 
		} 

		// close the connection 
		try
		{ 
			input.close(); 
			out.close(); 
			socket.close(); 
		} 
		catch(IOException i) 
		{ 
			System.out.println(i); 
		} 
	} 

	public static void main(String args[]) 
	{ 
		Scanner scanner = new Scanner(System.in);
		ClientSample client = new ClientSample(scanner.nextLine(), Integer.parseInt(scanner.nextLine())); 
	} 
} 
