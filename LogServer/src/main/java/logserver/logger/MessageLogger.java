package logserver.logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import logserver.server.ServerSession;
import logserver.server.UserSession;
import logserver.utils.Logger;

public class MessageLogger implements Runnable {

	private static final String DEFAULT_FILE_PATH = "D:\\JavaLogger\\messages.txt";
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

	private final UserSession userSession;
	private final String messagesFilePath;
	private final ServerSession serverSession;

	public MessageLogger(UserSession userSession, ServerSession serverSession) {
		this(userSession, serverSession, DEFAULT_FILE_PATH);
	}

	public MessageLogger(UserSession userSession, ServerSession serverSession, String filePath) {
		this.userSession = userSession;
		this.serverSession = serverSession;
		this.messagesFilePath = DEFAULT_FILE_PATH;
	}

	public void run() {
		try {
			Logger.logServer("User " + userSession.getCurrentUser() + " joined logger");
			writeMessageToFile(userSession.getCurrentUser(), userSession.getSocket());
			Logger.logServer("User " + userSession.getCurrentUser() + " left logger");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			serverSession.unregisterUser(userSession.getCurrentUser());
		}
	}

	private void writeMessageToFile(String currentUser, Socket socket) throws IOException {
		RandomAccessFile file = new RandomAccessFile(new File(messagesFilePath), "rw");
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String message;
		while ((message = in.readLine()) != null) {
			if (message != null) {
				synchronized (MessageLogger.class) {
					file.seek(file.length());
					while (message != null) {
						file.write(format(currentUser, message).getBytes());
						message = in.readLine();
					}
				}
			}
		}
		file.close();
	}

	private String format(String currentUser, String message) {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		return sdf.format(new Date()) + " [" + currentUser + "]: " + message + "\r\n";
	}

}