
public class Logger {
	public static boolean enabled = true;
	public static void print(Object sender, String str) {
		str = "LOG: "+sender.getClass().toString() + "->" + str;
		System.out.println(str);
	}
	public static void print( String str) {
		str = "LOG: " + str;
		System.out.println(str);
	}
}
