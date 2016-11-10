package server.library.log;

/**
 * Only to test LogHandler without starting the servers and the clients
 */
public class LogTester {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new LogHandler("LOG_8081").deleteConflitingLogs(3);
		
//		Entry[] logs = new LogHandler("LOG_8081").getEntriesSinceIndex(3);
//		for (Entry e : logs)
//			System.out.println(e.getEntry());
	}

}
