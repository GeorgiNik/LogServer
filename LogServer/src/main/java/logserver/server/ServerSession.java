package logserver.server;

import java.util.HashSet;
import java.util.Set;

public class ServerSession {

	private final Set<String> users;

	public ServerSession() {
		this.users = new HashSet<>();
	}

	public synchronized boolean registerUser(String username) {
		return username != null && users.add(username);
	}

	public synchronized void unregisterUser(String username) {
		if (users.contains(username))
			users.remove(username);
	}
}
