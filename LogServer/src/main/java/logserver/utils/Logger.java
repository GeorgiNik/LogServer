package logserver.utils;

public class Logger {

	public static void logServer(String message) {
		System.out.println("[Server]" + message);
	}

	public static void logServerError(String message) {
		System.err.println("[Server Error]" + message);
	}

	public static void logClient(String message) {
		System.out.println("[Client]" + message);
	}

	public static void logClientError(String message) {
		System.err.println("[Client Error]" + message);
	}
}
