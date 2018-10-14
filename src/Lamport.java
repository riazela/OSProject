import java.util.Hashtable;
import java.util.PriorityQueue;

public class Lamport {
	private static int serverId;
	private static PriorityQueue<Request> queue = new PriorityQueue<Request>();
	private static Hashtable<String, Request> currentRequests = new Hashtable<String, Request>();
	
	public static void setServerId(int id) {
		Lamport.serverId = id;
	}
	
	public static int getServerId() {
		return serverId;
	}
	
	
	
}
