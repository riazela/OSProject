package ClientSide;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
	public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException {
		Scanner scanner = new Scanner(System.in);
		System.out.println("writer or reader? (r/w)");
		String type = scanner.nextLine();
		System.out.println("Server IP:Port?");
		String[] ipPort = scanner.nextLine().split(":");
		Socket socket = new Socket(ipPort[0],Integer.parseInt(ipPort[1]));
		OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream()); 
		BufferedReader in = new BufferedReader( 
				new InputStreamReader(socket.getInputStream())); 
		if (type == "r") {
			for (int i = 0;i<100;i++) {
				out.write("req r\n");
				out.flush();
				System.out.println(in.readLine());
			}
			
		}
		else
		{
			System.out.println("Input file?");
			String path = scanner.nextLine();
			Scanner inputFile = new Scanner(new File(path));
			while (inputFile.hasNextLine()) {
				String line = inputFile.nextLine();
				out.write(line + "\n");
				System.out.println("sent request for " + line);
				System.out.println(in.readLine());
			}
			inputFile.close();
		}
		out.write("close");
		out.flush();
		socket.close();
	}
}
