import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
	
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
//		System.out.println("Enter the config file path");
//		String path = scanner.nextLine();
		if(args.length < 1) {
			System.out.println("Not enough args!");
			return;
		}
		String path = args[0];
		Scanner fileScanner;
		try {
			fileScanner = new Scanner(new File(path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
//		reading the id and the other servers
		int id = Integer.parseInt(fileScanner.nextLine());
		Lamport.setServerId(id);
		int port = Integer.parseInt(fileScanner.nextLine());
		int clientPort = Integer.parseInt(fileScanner.nextLine());
		ArrayList<String> serversAddress = new ArrayList<String>();
		while (fileScanner.hasNextLine()) {
			String line = fileScanner.nextLine();
			if (!line.equals(""))
				serversAddress.add(line);
		}
		fileScanner.close();

		Server.startListening(port);
		Server.ConnectToServers(serversAddress.toArray(new String[0]));
		System.out.println("Server started on port " + String.valueOf(port));
		System.out.println("waiting for connections");
		Client.waitForClient(clientPort);
		Lamport.startPulsing();
		while (!scanner.nextLine().equals("close"));
		Logger.print("closing everything");
		Lamport.stopPulsing();
		Server.stopEverything();
		Client.stopEverything();
		
	}
}
