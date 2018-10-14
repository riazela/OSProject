
public class Request implements Comparable<Request> {
	int serverId;
	int timeStamp;
	char requestType;
	RequestCallback callback;

	
	public Request(int serverId, int timeStamp, char requestType, RequestCallback requestCallback) {
		this.serverId = serverId;
		this.timeStamp = timeStamp;
		this.requestType = requestType;
		this.callback = requestCallback;
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
