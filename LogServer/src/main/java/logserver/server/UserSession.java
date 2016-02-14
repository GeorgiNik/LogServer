package logserver.server;

import java.net.Socket;

public class UserSession {

	private final String currentUser;
	private final Socket socket;

	public UserSession(Socket socket, String currentUser) {
		this.socket = socket;
		this.currentUser = currentUser;
	}

	public String getCurrentUser() {
		return currentUser;
	}

	public Socket getSocket() {
		return socket;
	}

}
