import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
	
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter the config file path");
		String path = scanner.nextLine();
		Scanner fileScanner;
		try {
			fileScanner = new Scanner(new File(path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
//		reading the id and the other servers
		int id = Integer.parseInt(fileScanner.nextLine());
		int port = Integer.parseInt(fileScanner.nextLine());
		ArrayList<String> serversAddress = new ArrayList<String>();
		while (fileScanner.hasNextLine()) {
			String line = fileScanner.nextLine();
			if (line != "")
				serversAddress.add(fileScanner.nextLine());
		}
		fileScanner.close();
	
		Server.ConnectToServers(serversAddress.toArray(new String[0]));
		System.out.println("waiting for connections");
		Server.startListening(port);
		while (scanner.nextLine()!="close");
		
		Server.stopEverything();
		
	}
}
