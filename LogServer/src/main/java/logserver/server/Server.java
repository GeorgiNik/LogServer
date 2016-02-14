package logserver.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import logserver.logger.MessageLogger;
import logserver.utils.HttpUtil;
import logserver.utils.Logger;

public class Server {

	private ServerSocket serverSocket;
	private ServerSession serverSession;

	public void start(int port) {
		serverSession = new ServerSession();

		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			Logger.logServerError("Server initialization failed");
			e.printStackTrace();
		}

		while (true) {
			try {
				Logger.logServer("Waiting at port: " + port + "...");
				Socket socket = serverSocket.accept();
				Logger.logServer("Client connected");
				startLoggerLogicThread(socket, serverSession);
			} catch (IOException e) {
				Logger.logServerError("Server execution interrupted");
			}
		}
	}

	private void startLoggerLogicThread(Socket socket, ServerSession serverSession) throws IOException {
		UserSession userSession = getUserSession(socket, serverSession);
		if (userSession == null) {
			sendResponseToClient(HttpUtil.STATUS_401, socket);
			return;
		}
		sendResponseToClient(HttpUtil.STATUS_200 + ", username:" + userSession.getCurrentUser(), socket);
		new Thread(new MessageLogger(userSession, serverSession)).start();
	}

	private UserSession getUserSession(Socket socket, ServerSession serverSession) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String currentUser = authenticateUser(in, serverSession);
			if (!currentUser.equals(""))
				return new UserSession(socket, currentUser);
		} catch (IOException e) {
			Logger.logServerError("Could not create user session");
		}
		return null;
	}

	private String authenticateUser(BufferedReader in, ServerSession session) {
		try {
			Logger.logServer("Authenticating user...");
			String currentUser = in.readLine();
			if (session.registerUser(currentUser))
				return currentUser;
		} catch (IOException e) {
			Logger.logServerError("Could not authenticating user");
		}
		return "";
	}

	private void sendResponseToClient(String response, Socket socket) {
		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream());
			out.println(response);
			out.flush();
			Logger.logServer("Sending response: " + response);
		} catch (IOException e) {
			Logger.logServerError("Could not send response");
		}
	}

}