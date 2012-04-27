package net.praqma.ava.util;

public class Utils {
	public static String getProgress( int total, int i ) {
		return "Progress: " + (i+1) + "/" + total;
	}
}
