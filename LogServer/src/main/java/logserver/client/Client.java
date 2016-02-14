package logserver.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import logserver.utils.HttpUtil;
import logserver.utils.Logger;
import logserver.utils.Messages;

public class Client implements Runnable {

	private final String URL;
	private final int PORT;
	private final String username;
	private final List<String> message;

	public Client(String url, int port) {
		URL = url;
		PORT = port;
		username = ManagementFactory.getRuntimeMXBean().getName();
		message = new ArrayList<>();
	}

	public void run() {
		try {
			Scanner scanner = new Scanner(System.in);
			Socket socket = connect();

			PrintWriter out;
			if ((out = logIn(socket, scanner)) == null)
				return;
			initializeChatSession(out, scanner, socket);

			out.close();
			socket.close();
			Logger.logClient("Session for user " + username + " terminated");
		} catch (IOException e) {
			Logger.logClientError("Client session interrupted, attempting to reconnect");
			run();
		}
	}

	private PrintWriter logIn(Socket socket, Scanner scanner) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

			out.println(username);
			out.flush();
			if (isUserAuthenticated(socket, username)) {
				Logger.logClient("Logged in as " + username);
				return out;
			}
		} catch (IOException e) {
			Logger.logClientError("Could not log in");
		}
		return null;
	}

	private Socket connect() {
		try {
			Socket socket = new Socket(URL, PORT);
			Logger.logClient("Connected to server: " + URL + ", port:" + PORT);
			return socket;
		} catch (IOException e) {
			Logger.logClientError("Could not connect to server: " + URL + ", port:" + PORT);
		}
		return null;
	}

	private void initializeChatSession(PrintWriter out, Scanner scanner, Socket socket) throws IOException {
		String line;
		Logger.logClient(Messages.ENTER_MESSAGE);
		while ((line = scanner.nextLine()) != null && !line.equals("-q")) {
			if (line.equals("-s"))
				sendMessage(out);
			else if (line.equals("-t")) {
				socket.close();
				throw new IOException();
			} else
				message.add(line);
		}
	}

	private void sendMessage(PrintWriter out) {
		message.stream().forEach(line -> out.println(line));
		out.flush();
		Logger.logClient(Messages.MESSAGE_SENT);
		Logger.logClient(Messages.ENTER_MESSAGE);
		message.clear();
	}

	private boolean isUserAuthenticated(Socket socket, String username) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String response = in.readLine();
		if (response.contains(HttpUtil.STATUS_200 + ", username:" + username))
			return true;
		Logger.logClient("User " + username + " was not authenticated (username is already in use)");
		return false;
	}
}
