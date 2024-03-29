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
import java.util.Iterator;

public class Server {
	
	public static Hashtable<Integer, Server> allServers = new Hashtable<Integer, Server>();
	public static boolean listeningForNewServers = true;
	public static int numberOfServers = 0;
	
	public static final String CLOSE_CODE = "close";
	public static final String REQ_CODE = "req";
	public static final String RELEASE_CODE = "release";
	public static final String ACK_CODE = "ack";
	public static final String ASKID_CODE = "give_me_id";
	public static final String GIVEID_CODE = "id";
	
	
	public static void startListening(int port) {
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
					
				while (listeningForNewServers) {
					Socket s;
					try {
						s = serverSocket.accept();
						Logger.print("someone connected");
						new Server(s, false);
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
		listeningForNewServers = false;
		Integer[] keys = allServers.keySet().toArray(new Integer[0]);
		for (Integer i:keys) {
			Server server = allServers.get(i);
			server.close();
		}
	}
	
	public static void ConnectToServers(String[] list) {
		String[] ipPort;
		numberOfServers = list.length;
		for (int i = 0; i < list.length; i++) {
			ipPort = list[i].split(":");
			String IP = ipPort[0];
			String port = ipPort[1];
			try {
				Socket s = new Socket(IP, Integer.parseInt(port));
				new Server(s, true);
				Logger.print("connected to "+ list[i]);
			} catch (NumberFormatException e) {
				System.out.println(e);
			} catch (UnknownHostException e) {
				Logger.print("cannot connect to "+ list[i]);
			} catch (IOException e) {
				Logger.print("cannot connect to "+ list[i]);
			}
		}
	}
	
	public static void sendReqToEveryone(int timestamp, String type) {
		// it will wait until the number os servers is not enough yet
		while (allServers.size() < numberOfServers);
		
		Integer[] keys = allServers.keySet().toArray(new Integer[0]);
		for (Integer i:keys) {
			Server server = allServers.get(i);
			server.sendRequest(timestamp, type);
		}
	}
	
	public static void sendReleaseToEveryone(int timestamp, String type) {
		Integer[] keys = allServers.keySet().toArray(new Integer[0]);
		for (Integer i:keys) {
			Server server = allServers.get(i);
			server.sendRelease(timestamp, type);
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
				outputStream.write(Clock.increment()+":"+"id:"+i+"\n");
				outputStream.flush();
			} catch (IOException e) {
				receivedCloseMessage();
			}
		}
	}
	
	public void receivedRequest(int timestamp, String type) {
		char chartype = type.charAt(0);
		Request req = new Request(this.id, timestamp, chartype, (t) -> {});
		Lamport.addNewRequest(req);
		sendAck(timestamp);
	}
	
	public void sendRequest(int timestamp, String type) {
		synchronized (outputStream) {
			try {
				String message  =Clock.increment()+":"+"request:"+timestamp+":"+type+"\n";
				Logger.print(this, "Sent message to " + id + "    " + message);
				outputStream.write(message);
				outputStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void receivedRelease(int timestamp) {
		Lamport.releaseRequest(timestamp, this.id);
	}
	
	public void sendRelease(int timestamp, String type) {
		synchronized (outputStream) {
			
			try {
				String message  = Clock.increment()+":"+"release:"+timestamp+":"+type+"\n";
				Logger.print(this, "Sent message to " + id + "    " + message);
				outputStream.write(message);
				outputStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
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
				outputStream.write(Clock.increment()+":"+"give_me_id\n");
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
			Clock.adjustTimer(Integer.parseInt(messageParts[0]));
			switch (messageParts[1]) {
			case "close":
				receivedCloseMessage();
				return;
			case "give_me_id":
				sendID();
				break;
			case "id":
				setID(Integer.parseInt(messageParts[2]));
				break;
			case "release":
				this.receivedRelease(Integer.parseInt(messageParts[2]));
				break;
			case "request":
				this.receivedRequest(Integer.parseInt(messageParts[2]), messageParts[3]);
				break;
			case "ack":
				this.receivedAck(Integer.parseInt(messageParts[2]));
				break;
			default:
				break;
			}
		}
	}
	
	
	public void receivedAck(int timestamp) {
		Lamport.receivedAck(timestamp);
	}
	
	public void sendAck(int timestamp) {
		synchronized (outputStream) {
			try {
				String message  = Clock.increment()+":"+"ack:"+timestamp+"\n";
				Logger.print(this, "Sent message to " + id + "    " + message);
				outputStream.write(message);
				outputStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void close() {
		if (socket_is_closed)
			return;
		socket_is_closed = true;
		try {
			synchronized (outputStream) {
				outputStream.write(Clock.increment()+":"+"close");
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
			e.printStackTrace();
		}
	}
}
