import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Server {
	
	int id;
	private Socket socket;
	private boolean socket_is_closed;
	private BufferedReader inputSteam;
	private OutputStreamWriter outputStream;
	private ServerCallback callBack;
	
	public Server(Socket socket, ServerCallback callBack) throws IOException {
		this.id = -1;
		this.socket = socket;
		this.callBack = callBack;
		
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
	}
	
	private void receivedCloseMessage() {
		socket_is_closed = true;
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		synchronized (callBack) {
			this.callBack.serverGotClosed();
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
		if (this.id == -1)
			this.id = id;
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
				return;
			case "id":
				setID(Integer.parseInt(messageParts[1]));
				return;
			default:
				break;
			}
		}
	}
	
	
	public void close() {
//		TODO: solve the problem of listener
		socket_is_closed = true;
		try {
			synchronized (outputStream) {
				outputStream.write("close");
				outputStream.flush();
			}
		} catch (IOException e) {
			
		}
		
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
