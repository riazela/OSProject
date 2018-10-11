import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Server {
	int id;
	private Socket socket;
	private DataInputStream inputSteam;
	private DataOutputStream outputStream;
	
	public Server(Socket socket) throws IOException {
		this.socket = socket;
		this.inputSteam = new DataInputStream( 
				new BufferedInputStream(socket.getInputStream())); 
		this.outputStream = new DataOutputStream(socket.getOutputStream());
	}
	
	
	
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			
		}
	}
}
