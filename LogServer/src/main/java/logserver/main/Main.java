package logserver.main;

import logserver.client.Client;
import logserver.server.Server;

public class Main {

	public static void main(String[] args) {
		new Thread(new Client("localhost", 10514)).start();
		new Thread(() -> new Server().start(10514)).start();
	}

}
