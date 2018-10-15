import java.util.Hashtable;
import java.util.PriorityQueue;

public class Lamport {
	private static int serverId;
	private static PriorityQueue<Request> queue = new PriorityQueue<Request>();
	private static Hashtable<String, Request> currentRequests = new Hashtable<String, Request>();
	private static char currentRequestType = 'r';
	private static Hashtable<Integer, Request> requestsWaitingForAck = new Hashtable<Integer, Request>();
	
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
	public static Request addNewRequest(Request request) {
		synchronized (queue) {
			//if the request is coming from a client inside then we need to send request to all servers
			if (request.serverId == serverId) {
				request.timeStamp = Clock.increment();
				Server.sendReqToEveryone(request.timeStamp, ""+ request.requestType);
				requestsWaitingForAck.put(request.timeStamp, request);
			}
			else {
				queue.add(request);
			}
			pulse();
		}
		return request;
	}
	
	/***
	 * This method is evoked after any incident related to requests to check if any request is entering into the critical section
	 */
	public static void pulse() {
		if (queue.isEmpty())
			return;
		
		if (currentRequests.isEmpty()) {
			Request req = queue.poll();
			currentRequestType = req.requestType;
			currentRequests.put(req.timeStamp + ":" + req.serverId, req);
			req.callback.grantAccess(req.requestType);
		}
		
		// if the current access if read then we give access to all recent reads
		if (currentRequestType=='r') {
			while (!queue.isEmpty() && queue.peek().requestType=='r') {
				Request req = queue.poll();
				currentRequests.put(req.timeStamp + ":" + req.serverId, req);
				req.callback.grantAccess(req.requestType);
			}
		}
	}
	
	public static void receivedAck(int timestamp) {
		synchronized (queue) {
			Request req = requestsWaitingForAck.get(timestamp);
			req.numberOfAcks++;
			if (req.numberOfAcks >= Server.numberOfServers) {
				requestsWaitingForAck.remove(timestamp);
				queue.add(req);
				pulse();
			}
		}
	}
	
	/***
	 * This method releases some request. It can be called for a request of another server or from a client connected to the current server
	 * @param timestamp time stamp of the request
	 * @param serverID ServerID of the request (this is the same as Lamport.serverId if it is called from a client inside 
	 */
	public static void releaseRequest(int timestamp, int serverID) {
		synchronized (queue) {
			Request request = currentRequests.get(timestamp + ":" + serverID);
			currentRequests.remove(timestamp + ":" + serverID);
			
			// if the request is released from our client then we need to notify others
			if (serverID == Lamport.serverId)
			{
				Server.sendReleaseToEveryone(timestamp, request.requestType +"");
			}
			pulse();
		}
	}
	
}
