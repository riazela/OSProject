import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

public class Client implements RequestCallback{
	public static ArrayList<Client> allClients = new ArrayList<Client>();
	public static ArrayList<Request> allRequests = new ArrayList<Request>();
	private static boolean waitingForNewClients = true;
	private static String sharedFile="output.txt";
	
		
	public static void waitForClient(int port) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				ServerSocket serverSocket;
				try {
					 serverSocket = new ServerSocket(port);
					 serverSocket.setSoTimeout(1000);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
					
				while (waitingForNewClients) {
					Socket s;
					try {
						s = serverSocket.accept();
						Logger.print("one client connected");
						allClients.add(new Client(s));
					}
					catch (SocketTimeoutException e){
						continue;
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
		
	}
	
	public static void stopEverything() {
		waitingForNewClients = false;
		for (Client c: allClients) {
			c.close();
		}
	}
	
	
	private Socket socket;
	private BufferedReader inputSteam;
	private OutputStreamWriter outputStream;
	private boolean hasPendingReq;
	private boolean isGranted;
	private int currentReqTS;
	private int currentWriteVal;
	
	
	public Client(Socket socket) throws IOException {
		this.socket = socket;
		this.hasPendingReq = false;
		this.isGranted = false;
		this.currentReqTS = -1;
		this.currentWriteVal = 0;
		
		socket.setSoTimeout(1000);
		
		this.inputSteam = new BufferedReader( 
				new InputStreamReader(socket.getInputStream())); 
		this.outputStream = new OutputStreamWriter(socket.getOutputStream());

		this.listen();
	}
	
	public void listen () throws IOException {
		String messagestr;
		while (true) {
			try {
				messagestr = inputSteam.readLine();
				Logger.print(this, "Received message from clinet on " + this.socket.getPort() + "    " + messagestr);
			} catch (SocketTimeoutException e) {
				continue;
			} catch (IOException e) {
				this.close();
				return;
			} 
			String[] messageParts = messagestr.split(" ");
			switch (messageParts[0]) {
			case "req":
				if(hasPendingReq) {
					System.out.println("Closing the client on port " + this.socket.getPort() + ", request # exceeded one");
					outputStream.write("No more than one request is allowed at a time! \n");
					outputStream.flush();
					this.close();
					return;
				}
				char type = 0;
				try {
					type = messageParts[1].charAt(0);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					this.close();
					return;
				}
				if (type != 'r' && type != 'w') {
					System.out.println("Closing the client on port " + this.socket.getPort() + ", invalid request type");
					outputStream.write("Invalid request type! \n");
					outputStream.flush();
					this.close();
					return;
				}
				if (type == 'w') {
					try {
						currentWriteVal = messageParts[2].charAt(0);
					} catch (Exception e) {
						System.out.println("Closing the client on port " + this.socket.getPort() + ", no value given for write");
						outputStream.write("No given value for write! \n");
						outputStream.flush();
						// TODO: handle exception
						this.close();
						return;
					}
				}
				sendReq(type);
				this.hasPendingReq = true;
				outputStream.write("Request sent to Server \n");
				outputStream.flush();
				break;
//			case "release":
//				if(!hasPendingReq) {
//					System.out.println("Closing the client on port " + this.socket.getPort() + ", released while has no request");
//					outputStream.write("Has no pending request to release! \n");
//					outputStream.flush();
//					this.close();
//					return;	
//				}
//				else if(!isGranted) {
//					System.out.println("Closing the client on port " + this.socket.getPort() + ", released while not granted");
//					outputStream.write("Has no granted request to release! \n");
//					outputStream.flush();
//					this.close();
//					return;
//				}
//				
//				release();
//				break;
			case "close":
				this.close();
				return;
			default:
				break;
			}
		}
	}	
	
	public void sendReq(char type) throws IOException {
		Request r = Lamport.addNewRequest(new Request(Lamport.getServerId(), 0, type, this));
		this.currentReqTS = r.timeStamp;	
	}
	
	
	public void release() {
		this.hasPendingReq = false;
		this.isGranted = false;
		try {
			Lamport.releaseRequest(currentReqTS, Lamport.getServerId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}	
	
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void grantAccess(char type) {
		// TODO Auto-generated method stub
		this.isGranted = true;
		try {
			outputStream.write("Granted \n");
			outputStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Request granted, Accessing the shared file... \n");
		
		if (type == 'w') {
			try {
				Scanner in = new Scanner(new FileReader(sharedFile));
				String last = "";
				String line = "";
				while ((line = in.nextLine()) != null) { 
					last = line;
				}
				in.close();
				String[] lastArr = last.split(":");
				Thread.sleep(500);
				FileWriter fw = new FileWriter(sharedFile, true);
				int lastStamp = Integer.parseInt(lastArr[1]);
				int lastSum = Integer.parseInt(lastArr[0]);
				int newStamp = lastStamp + 1;
				int newSum = lastSum + currentWriteVal;
				fw.append(Integer.toString(newStamp)+":");
				Thread.sleep(500);
				fw.append(Integer.toString(newSum) + "\n");
				fw.close();
				
				try {
					outputStream.write("Write done successfully! \n");
					outputStream.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			} catch (Exception e) {
				// TODO: handle exception
				this.close();
				return;
			}
			
		}
		
		this.release();
		
		return;
	}
	
	
	
	

}
