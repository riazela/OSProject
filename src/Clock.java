public class Clock {
	
	private static Integer i = 0;
	public void increment() {
		synchronized (i) {
			i++;
		}
	}
	
	public Integer getTime() {
		synchronized (i) {
			return i;
		}
	}

}
