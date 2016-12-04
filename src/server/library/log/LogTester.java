package server.library.log;

import java.io.IOException;

/**
 * Only to test LogHandler without starting the servers and the clients
 */
public class LogTester {

	public static void main(String[] args) throws IOException {
		
		LogHandler logHandler = new LogHandler("LOG_localhost_8081");
//		System.out.println("hasEntry? "+ logHandler.hasEntry(1, 0));
	//	System.out.println("Empty? "+ logHandler.isLogEmpty());
	//	System.out.println("n linhas: "+logHandler.getCurrentLogIndex());
	//	System.out.println("Linha 8 committed? "+logHandler.commitLogEntry(8));
	//	System.out.println("Tem entry 2,1 ? " +logHandler.hasEntry(2, 1));
	//	System.out.println("->"+logHandler.getLastLogEntry().toString());
	//	logHandler.removeEntrysAfterIndex(5);
		
	}

}
