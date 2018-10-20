package ClientSide;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Client {
	public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException {

		// Program to calculate execution time or elapsed time in Java

		long startTime = 0;
		long endTime = 0;
		long timeElapsed = 0;
		

		
//		Scanner scanner = new Scanner(System.in);
//		System.out.println("writer or reader or time? (r/w/t)");
//		String type = scanner.nextLine();
		if(args.length < 2) {
			System.out.println("Not enough args!");
			return;
		}
		String type = args[0];
//		System.out.println("Server IP:Port?");
//		String[] ipPort = scanner.nextLine().split(":");
		String[] ipPort = args[1].split(":");
		Socket socket = new Socket(ipPort[0],Integer.parseInt(ipPort[1]));
		OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream()); 
		BufferedReader in = new BufferedReader( 
				new InputStreamReader(socket.getInputStream())); 
		
		// ... the code being measured s tarts ...
		startTime = System.currentTimeMillis();
		if (type.equals("r")) {
			for (int i = 0;i<100;i++) {
				out.write("req r\n");
				out.flush();
				System.out.println(in.readLine());
			}
		}
		else if (type.equals("w"))
		{
//			System.out.println("Input file?");
//			String path = scanner.nextLine();
			if(args.length < 3) {
				System.out.println("Not enough args!");
				return;
			}
			String path = args[2];
			Scanner inputFile = new Scanner(new File(path));
			while (inputFile.hasNextLine()) {
				String line = inputFile.nextLine();
				out.write("req w "+line + "\n");
				out.flush();
				System.out.println("sent request for " + line);
				System.out.println(in.readLine());
			}
			inputFile.close();
			
			// ... the code being measured ends ...
			endTime = System.currentTimeMillis();
			timeElapsed = endTime - startTime;
			System.out.println("Execution time with Lamport in milliseconds: " + timeElapsed);
		}
		else if (type.equals("t"))
		{
//			System.out.println("Input file?");
//			String path = scanner.nextLine();
			if(args.length < 3) {
				System.out.println("Not enough args!");
				return;
			}
			String path = args[2];
			Scanner inputFile = new Scanner(new File(path));
			while (inputFile.hasNextLine()) {
				String line = inputFile.nextLine();
				out.write("t\n");
				out.flush();
				System.out.println("wrting without lamport " + line);
				System.out.println(in.readLine());
			}
			inputFile.close();
			
			// ... the code being measured ends ...
			endTime = System.currentTimeMillis();
			timeElapsed = endTime - startTime;
			System.out.println("Execution time WITHOUT Lamport in milliseconds: " + timeElapsed);
		}
		out.write("close");
		out.flush();
		socket.close();
		
	}
}
