package ClientSide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class RequestDistributor {
	public static void main(String[] args) throws IOException {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enther the path for all requests");
		String path = scanner.nextLine();
		System.out.println("Number of clients:");
		int numberOfClients = Integer.parseInt(scanner.nextLine());
		
		Scanner mainFile = new Scanner(new File(path));
		FileWriter[] writers = new FileWriter[numberOfClients];
		for (int i = 0; i < writers.length; i++) {
			writers[i] = new FileWriter(new File("requests_"+i+".txt"));
		}
		
		while (mainFile.hasNextLine()) {
			String line = mainFile.nextLine();
			Random rand = new Random();
			writers[rand.nextInt(numberOfClients)].write(line+"\n");
		}
		
		for (int i = 0; i < writers.length; i++) {
			writers[i].close();
		}
		mainFile.close();
		System.out.println("done");
	}
}
