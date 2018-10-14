public class Clock {
	
	private static Integer i = 0;
	public static int increment() {
		synchronized (i) {
			i++;
			return i;
		}
	}
	
	public static int adjustTimer(int time) {
		synchronized (i) {
			i = Integer.max(i, time)+1;
			return i;
		}
	}

}
