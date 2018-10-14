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
	
	
	/***
	 * This method is called whenever there is a request for critical section (either from inside or outside)
	 * @param request is the request for critical section
	 */
	public static void addNewRequest(Request request) {
		
	}
	
	/***
	 * This method releases some request. It can be called for a request of another server or from a client connected to the current server
	 * @param timestamp time stamp of the request
	 * @param serverID ServerID of the request (this is the same as Lamport.serverId if it is called from a client inside 
	 */
	public static void releaseRequest(int timestamp, int serverID) {
		
	}
	
}
