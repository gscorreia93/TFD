package server.library.log;

import java.util.UUID;

import server.library.Entry;

/**
 * Only to test LogHandler without starting the servers and the clients
 */
public class LogTester {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Entry[] entries = new Entry[]{new Entry(UUID.randomUUID().toString(),"asd","asd")};
		
		new LogHandler("LOG_8081").writeLogEntry(entries, 1);
		
		new LogHandler("LOG_8082").writeLogEntry(entries, 1);
		
//		Entry[] logs = new LogHandler("LOG_8081").getEntriesSinceIndex(3);
//		for (Entry e : logs)
//			System.out.println(e.getEntry());
	}

}
