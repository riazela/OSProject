import java.util.Hashtable;
import java.util.PriorityQueue;

public class Lamport {
	private static int serverId;
	private static PriorityQueue<Request> queue = new PriorityQueue<Request>();
	private static Hashtable<String, Request> currentRequests = new Hashtable<String, Request>();
	private static Hashtable<String, Request> deletedRequests = new Hashtable<String, Request>();
	private static char currentRequestType = 'r';
	private static Hashtable<Integer, Request> requestsWaitingForAck = new Hashtable<Integer, Request>();
	private static boolean pulsing = true;
	private static Object lock = new Object();
	
	public static void setServerId(int id) {
		Lamport.serverId = id;
	}
	
	public static int getServerId() {
		return serverId;
	}
	
	public static void stopPulsing() {
		pulsing = false;
	}
	
	public static void startPulsing() {
		pulsing = true;
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				pulse();
	
			}
		});
		t.start();
	}
	
	/***
	 * This method is called whenever there is a request for critical section (either from inside or outside)
	 * @param request is the request for critical section
	 */
	public static Request addNewRequest(Request request) {
		synchronized (queue) {
			//if the request is coming from a client inside then we need to send request to all servers
			if (request.serverId == Lamport.serverId) {
				request.timeStamp = Clock.increment();
				Server.sendReqToEveryone(request.timeStamp, ""+ request.requestType);
				requestsWaitingForAck.put(request.timeStamp, request);
			}
			else {
				queue.add(request);
			}
			
		}
		
		
		return request;
	}
	
	/***
	 * This method is evoked after any incident related to requests to check if any request is entering into the critical section
	 */
	public static void pulse() {
		lock = new Object();
		while (pulsing) {
			
			synchronized (queue) {
				if (!queue.isEmpty()) {
//					Logger.print("queue is not empty");
					if (currentRequests.isEmpty() && requestsWaitingForAck.isEmpty()) {
//						Logger.print("There is not current request");
						
						
						Request req = queue.poll();
						if (!deletedRequests.containsKey(req.timeStamp+":"+req.serverId))
						{
							Logger.print("Request for execution is " + req.serverId + " " + req.timeStamp);
							currentRequestType = req.requestType;
							currentRequests.put(req.timeStamp + ":" + req.serverId, req);
							Thread t = new Thread(new Runnable() {
								
								@Override
								public void run() {
									req.callback.grantAccess(req.requestType);
								}
							});
							t.start();
						}
						else
						{
							currentRequestType = 'w';
							deletedRequests.remove(req.timeStamp+":"+req.serverId);
						}
					}
					
					// if the current access if read then we give access to all recent reads
					if (currentRequestType=='r' && requestsWaitingForAck.isEmpty()) {
						while (!queue.isEmpty() && queue.peek().requestType=='r') {
							Logger.print("current request is read and adding another request");
							Request req = queue.poll();
							if (!deletedRequests.containsKey(req.timeStamp+":"+req.serverId))
							{
								currentRequests.put(req.timeStamp + ":" + req.serverId, req);
								Thread t = new Thread(new Runnable() {
									
									@Override
									public void run() {
										req.callback.grantAccess(req.requestType);
									}
								});
								t.start();
							}
							else
							{
								deletedRequests.remove(req.timeStamp+":"+req.serverId);
							}
						}
					}
				}
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				
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
			}
			Logger.print("Received Ack for timestamp "+ timestamp);
		}
		
		
		
	}
	
	/***
	 * This method releases some request. It can be called for a request of another server or from a client connected to the current server
	 * @param timestamp time stamp of the request
	 * @param serverID ServerID of the request (this is the same as Lamport.serverId if it is called from a client inside 
	 */
	public static void releaseRequest(int timestamp, int serverID) {
		synchronized (queue) {
			Logger.print("Received Release for timestamp "+ timestamp);
			Request request = currentRequests.get(timestamp + ":" + serverID);
			if (request==null) {
				deletedRequests.put(timestamp + ":" + serverID, new Request(0, 0, 'w', null));
			}
			else
				currentRequests.remove(timestamp + ":" + serverID);
			
			// if the request is released from our client then we need to notify others
			if (serverID == Lamport.serverId)
			{
				Server.sendReleaseToEveryone(timestamp, request.requestType +"");
			}
		}
		

	}
	
}
