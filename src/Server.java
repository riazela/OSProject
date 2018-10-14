import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;

public class Server {
	
	public static Hashtable<Integer, Server> allServers;
	public static boolean listeningForNewServers = true;
	public static int numberOfServers = 0;
	
	public static void startListening(int port) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				ServerSocket serverSocket;
				try {
					 serverSocket = new ServerSocket(port);
					 serverSocket.setSoTimeout(1000);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
					
				while (listeningForNewServers) {
					Socket s;
					try {
//							TODO: check if we need
						s = serverSocket.accept();
						new Server(s, false);
					}
					catch (SocketTimeoutException e){
						continue;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}
		});
		t.start();
	}
	
	public static void ConnectToServers(String[] list) {
		String[] ipPort;
		for (int i = 0; i < list.length; i++) {
			ipPort = list[i].split(":");
			String IP = ipPort[0];
			String port = ipPort[1];
			try {
				Socket s = new Socket(IP, Integer.parseInt(port));
				new Server(s, true);
				System.out.println("connected to "+ list[i]);
			} catch (NumberFormatException e) {
				System.out.println(e);
			} catch (UnknownHostException e) {
				System.out.println("cannot connect to "+ list[i]);
			} catch (IOException e) {
				System.out.println("cannot connect to "+ list[i]);
			}
		}
	}
	
	
	
	// Object Level Part
	int id;
	private Socket socket;
	private boolean socket_is_closed;
	private BufferedReader inputSteam;
	private OutputStreamWriter outputStream;
	private boolean initiatedFromUs;
	
	public Server(Socket socket, boolean initiatedFromUs) throws IOException {
		this.id = -1;
		this.socket = socket;
		this.initiatedFromUs = initiatedFromUs;
		
		socket.setSoTimeout(1000);
		socket_is_closed = false;
		
		this.inputSteam = new BufferedReader( 
				new InputStreamReader(socket.getInputStream())); 
		this.outputStream = new OutputStreamWriter(socket.getOutputStream());
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				Server.this.listen();
			}
		});
		t.start();
		
		this.askForID();
	}
	
	public boolean isInitiatedFromUs() {
		return initiatedFromUs;
	}
	
	private void receivedCloseMessage() {
		socket_is_closed = true;
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (this.id != -1) {
			allServers.remove(this.id, this);
		}
	}
	
	private void sendID() {
		int i = Lamport.getServerId();
		Logger.print(this, "Sending my id to server with id  " + this.id);
		synchronized (outputStream) {
			try {
				outputStream.write("id:"+i+"\n");
				outputStream.flush();
			} catch (IOException e) {
				receivedCloseMessage();
			}
		}
	}
	
	private void setID(int id) {
		if (this.id == -1) {
			this.id = id;
			if ((Lamport.getServerId() < id && this.initiatedFromUs) || 
					(Lamport.getServerId() > id && !this.initiatedFromUs)) {
				if (allServers.get(id) != this && allServers.get(id) != null)
					allServers.get(id).close();
				allServers.put(id, this);
			} else {
				if (allServers.get(id) == null || allServers.get(id) == this)
					allServers.put(id, this);
				else
					this.close();
			}
		}
		else if (this.id != id) {
			Logger.print(this, "Error: received set id command from  " + this.id + "  to set   " + id);
			receivedCloseMessage();
		}
	}
	
	private void askForID() {
		synchronized (outputStream) {
			try {
				outputStream.write("give_me_id\n");
				outputStream.flush();
			} catch (IOException e) {
				receivedCloseMessage();
			}
		}
	}
	
	public void listen() {
		String messagestr;
		
		while (!socket_is_closed) {
			try {
				messagestr = inputSteam.readLine();
				Logger.print(this, "Received message from " + id + "    " + messagestr);
			} catch (SocketTimeoutException e) {
				continue;
			} catch (IOException e) {
				receivedCloseMessage();
				return;
			} 
			String[] messageParts = (messagestr.split("\n"))[0].split(":");
			switch (messageParts[0]) {
			case "close":
				receivedCloseMessage();
				return;
			case "give_me_id":
				sendID();
				break;
			case "id":
				setID(Integer.parseInt(messageParts[1]));
				break;
			default:
				break;
			}
		}
	}
	
	
	public void close() {
		if (socket_is_closed)
			return;
		socket_is_closed = true;
		try {
			synchronized (outputStream) {
				outputStream.write("close");
				outputStream.flush();
			}
		} catch (IOException e) {
			
		}
		
		if (this.id != -1) {
			allServers.remove(this.id, this);
		}
		
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
