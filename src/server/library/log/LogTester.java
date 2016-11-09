package server.library.log;

/**
 * Only to test LogHandler without starting the servers and the clients
 */
public class LogTester {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new LogHandler("LOG_8081").containsLogRecord(1, 1);
	}

}
