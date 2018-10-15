
public class Request implements Comparable<Request> {
	int serverId;
	int timeStamp;
	char requestType;
	RequestCallback callback;
	int numberOfAcks;

	
	/***
	 * 
	 * @param serverId: is the same as Lamport.serverId if the request is from a client
	 * @param timeStamp: timestamp of the request
	 * @param requestType: this is the request type, it is either 'r' or 'w'
	 * @param requestCallback: this is the client who has asked for the request
	 */
	public Request(int serverId, int timeStamp, char requestType, RequestCallback requestCallback) {
		this.serverId = serverId;
		this.timeStamp = timeStamp;
		this.requestType = requestType;
		this.callback = requestCallback;
		this.numberOfAcks = 0;
	}


	@Override
	public int compareTo(Request o) {
		if (this.timeStamp>o.timeStamp)
			return 1;
		else if (this.timeStamp<o.timeStamp)
			return -1;
		else if (this.serverId>o.serverId)
			return 1;
		else
			return -1;
	}
	
}
