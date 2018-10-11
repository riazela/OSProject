
public class Lamport {
	private static int serverId;
	
	public static void setServerId(int id) {
		Lamport.serverId = id;
	}
	
	public static int getServerId() {
		return serverId;
	}
}
