package server.library.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;

import server.library.Entry;
import server.library.Response;

/**
 * Class that handles the logs.
 */
public class LogHandler {

	private RandomAccessFile logFile;
	private File fileLog;

	public LogHandler(String filename) {

		fileLog = new File(filename);
		if (!fileLog.exists()) {
			try {
				fileLog.createNewFile();
			} catch (IOException e) {
				System.out.println("cannot create log file");
				e.printStackTrace();
			}
		} else {
			System.out.println("LogFile already exist");
		}
		try {
			logFile = new RandomAccessFile(fileLog, "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Follower writes a client log received
	 * from the leader.
	 * 
	 * If an existing entry conflicts with a new one (same index
	 * 	but different terms), delete the existing entry and all
	 * 	that follow it (§5.3)
	 * Append any new entries not already in the log
	 * If leaderCommit > commitIndex, set commitIndex =
	 * 	min(leaderCommit, index of last new entry)
	 * 
	 * @return
	 * 1. false if term < currentTerm (§5.1)
	 * 2. false if log doesn’t contain an entry at prevLogIndex
	 * 		whose term matches prevLogTerm (§5.3)
	 */
	public Response followerReplication(int term, int leaderId,
			int prevLogIndex, int prevLogTerm, Entry[] entries, int leaderCommit, int thisTerm) {

		Response response;

		// Writes log
		try {
			
			if (thisTerm < term || hasEntry(prevLogIndex, prevLogTerm)) {
				// TODO if (thisTerm < term) update term
				System.out.println(prevLogIndex + " " + prevLogTerm);
				// If an existing entry conflicts with a new one (same index but 
				// different terms), delete the existing entry and all that follow it (§5.3)

				//TODO
				//			deleteConflitingLogs(prevLogIndex);

				LogEntry lastLog = getLastLogEntry();

				response = new Response(thisTerm, false);
				response.setLogDeprecated();
				response.setLastLogTerm(lastLog.getLogTerm());
				response.setLastLogIndex(lastLog.getLogIndex());
				return response;
			}
			
			writeLogEntries(entries, term);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new Response(thisTerm, true);
	}
	
	public void leaderReplication(int term, int leaderId,
			int prevLogIndex, int prevLogTerm, Entry[] entries, int leaderCommit, int thisTerm) {

		// Writes log
		try {
			
			if (thisTerm < term || !hasEntry(prevLogIndex, prevLogTerm)) {
				// TODO if (thisTerm < term) update term

				// If an existing entry conflicts with a new one (same index but 
				// different terms), delete the existing entry and all that follow it (§5.3)
				
				// TODO
				//deleteConflitingLogs(prevLogIndex);
			}
			
			writeLogEntries(entries, term);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Writes all log entries received
	 * 
	 * @return the log entry index
	 * @throws IOException
	 */
	public void writeLogEntries(Entry[] entries, int logTerm) throws IOException {
		for (int i = 0; i < entries.length; i++) {
			writeLogEntry(entries[i], logTerm);
		}
	}

	/**
	 * Append the entry to end of log file
	 * 
	 * @param entry
	 * @param logTerm
	 * @throws IOException
	 */
	private void writeLogEntry(Entry entry, int logTerm) throws IOException {

		int term = entry.getTerm() > 0 ? entry.getTerm() : logTerm;
		LogEntry newLogEntry = new LogEntry(getLastLogIndex(), term, entry.getEntry(), entry.getClientID());
		logFile.seek(fileLog.length());
		logFile.writeBytes(newLogEntry.toString());
	}

	/**
	 * Commits a log entry
	 * 
	 * @throws IOException
	 */
	public boolean commitLogEntry(int logEntryIndex) throws IOException {

		if (logEntryIndex > getLastLogIndex()) {
			return false;
		}

		logFile.seek(0);

		for (int i = 0; i < logEntryIndex - 1; i++) {
			logFile.readLine();
		}
		long pointer = logFile.getFilePointer();
		String line = logFile.readLine();
		if (line.length() < 1) {
			return false;
		}

		LogEntry aux = new LogEntry();
		StringBuilder sb = new StringBuilder(line);
		int lastDelimiter = sb.lastIndexOf(aux.getDelimiter());
		sb.replace(lastDelimiter + 1, sb.length(), "true ");
		logFile.seek(pointer);
		logFile.writeBytes(sb.toString());
		return true;
	}

	/**
	 * Get last log index (current)
	 */
	public int getLastLogIndex() throws IOException {
		int countLines = 0;
		logFile.seek(0);
		while (logFile.readLine() != null) {
			countLines++;
		}
		return countLines;
	}

	public int getLastCommitedLogIndex() {
		List<LogEntry> logs;
		try {
			logs = getAllEntriesAfterIndex(0);

			for (int i = logs.size() - 1; i >= 0; i--) {
				if (logs.get(i).isCommited()) {
					return logs.get(i).getLogIndex();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return 0;
	}

	/**
	 * Check if log file is empty
	 * 
	 * @return false if file is not empty
	 * @throws IOException
	 */
	public boolean isLogEmpty() throws IOException {
		return logFile.length() == 0;
	}

	/**
	 * Check if Entry with logIndex on logTerm exists in log file
	 * 
	 * @param logIndex
	 * @param logTerm
	 * @return true if entry exist in log file
	 * @throws IOException
	 */
	public boolean hasEntry(int logIndex, int logTerm) throws IOException {
		if (isLogEmpty() || logIndex == 0 || logTerm == 0) {
			return false;
		}

		logFile.seek(0);
		String line;
		LogEntry entry = new LogEntry();
		while ((line = logFile.readLine()) != null) {
			if (line.startsWith(logIndex + entry.getDelimiter() + logTerm)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Iterate all log file to get the last entry
	 * 
	 * @return
	 * @throws IOException
	 */
	public LogEntry getLastLogEntry() throws IOException {
		logFile.seek(0);
		String line;
		while ((line = logFile.readLine()) != null) {
			try {
				return new LogEntry(line);
			} catch (Exception e) { // para o caso de haver uma linha mal
									// feita/vazia/...
				return new LogEntry();
			}
		}
		return new LogEntry();
	}

	/**
	 * Remove all entrys after index (index excluded)
	 * 
	 * @param logIndex
	 * @throws IOException
	 */
	public void removeEntrysAfterIndex(int logIndex) throws IOException {
		if (logIndex > 0) {
			logFile.seek(0);
			StringBuilder sb = new StringBuilder();

			for (int i = 0; i < logIndex; i++) {
				sb.append(logFile.readLine() + "\n");
			}
			sb.deleteCharAt(sb.lastIndexOf("\n"));
			logFile.setLength(0);
			logFile.writeBytes(sb.toString());
		}
	}

	/**
	 * Get a List of Entries with all entries after logIndex (logIndex excluded)
	 * 
	 * @param logIndex
	 * @return
	 * @throws IOException
	 */
	public List<LogEntry> getAllEntriesAfterIndex(int logIndex) throws IOException {

		List<LogEntry> missedEntrys = new LinkedList<LogEntry>();

		String line;
		logFile.seek(0);
		LogEntry le = new LogEntry();

		// posicionar o logFile para a ultima entry "valida"
		for (int i = 0; i < logIndex; i++) {
			logFile.readLine();
		}

		while ((line = logFile.readLine()) != null) {
			le = new LogEntry(line);
			missedEntrys.add(le);
		}

		return missedEntrys;
	}

}
/// **
// * Follower writes a client log received
// * from the leader.
// *
// * If an existing entry conflicts with a new one (same index
// * but different terms), delete the existing entry and all
// * that follow it (Â§5.3)
// * Append any new entries not already in the log
// * If leaderCommit > commitIndex, set commitIndex =
// * min(leaderCommit, index of last new entry)
// *
// * @return
// * 1. false if term < currentTerm (Â§5.1)
// * 2. false if log doesnâ€™t contain an entry at prevLogIndex
// * whose term matches prevLogTerm (Â§5.3)
// */
// public Response followerReplication(int term, int leaderId, int prevLogIndex,
/// int prevLogTerm, Entry[] entries, int leaderCommit, int thisTerm) {
//
// Response response;
//
// if (thisTerm < term || !containsLogRecord(prevLogIndex, prevLogTerm)) {
// // TODO if (thisTerm < term) update term
//
// // If an existing entry conflicts with a new one (same index but
// // different terms), delete the existing entry and all that follow it (Â§5.3)
// deleteConflitingLogs(prevLogIndex);
//
// LogEntry lastLog = getLastLog();
//
// response = new Response(thisTerm, false);
// response.setLogDeprecated();
// response.setLastLogTerm(lastLog.getLogTerm());
// response.setLastLogIndex(lastLog.getLogIndex());
// return response;
// }
//
// // Writes log
// writeLogEntry(entries, term);
//
// return new Response(thisTerm, true);
// }





/*public Response commitLog(int leaderCommit, int thisTerm) {
String commitedLog = lh.commitLogEntry(leaderCommit);

System.out.println(leaderCommit + ": Commiting '" + commitedLog + "' on " + server.getPort() + " as a "
		+ server.getState() + " at " + leaderCommit);

return new Response(thisTerm, true);
}*/