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
	private LogEntry lastLogEntry;

	public LogHandler(String filename) {

		fileLog = new File(filename);
		if (!fileLog.exists()) {
			try {
				fileLog.createNewFile();
			} catch (IOException e) {
				System.err.println("Cannot create log file!");
				e.printStackTrace();
			}
		} else {
			System.err.println("LogFile already exist");
		}

		try {
			logFile = new RandomAccessFile(fileLog, "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		findLastLogEntry();
	}

	/**
	 * Follower writes a client log received
	 * from the leader.
	 * 
	 * If an existing entry conflicts with a new one (same index
	 * 	but different terms), delete the existing entry and all
	 * 	that follow it (�5.3)
	 * Append any new entries not already in the log
	 * If leaderCommit > commitIndex, set commitIndex =
	 * 	min(leaderCommit, index of last new entry)
	 * 
	 * @return false if log doesn't contain an 
	 *   entry at prevLogIndex whose term matches prevLogTerm (�5.3)
	 */
	public Response followerReplication(int term, int prevLogIndex, int prevLogTerm, Entry[] entries, int leaderCommit) {

		Response response;
		String requestID = entries.length > 0 ? entries[entries.length - 1].getRequestID() : "NOT_FOUND";

		if (!hasEntry(prevLogIndex, prevLogTerm)) {
			System.out.println("DEPRECATED: " + prevLogIndex + " & " + prevLogTerm);

			// If an existing entry conflicts with a new one (same index but 
			// different terms), delete the existing entry and all that follow it (�5.3)
			removeEntriesAfterIndex(prevLogIndex);

			LogEntry lastLog = getLastLogEntry();

			response = new Response(term, false);
			response.setLogDeprecated();
			response.setRequestID(requestID);
			response.setLastLogTerm(lastLog.getLogTerm());
			response.setLastLogIndex(lastLog.getLogIndex());
			return response;
		}

		writeLogEntries(entries, term);

		response = new Response(term, true);
		response.setRequestID(requestID);
		
		return response;
	}

	/**
	 * Writes all entries received on end of log
	 */
	public int[] writeLogEntries(Entry[] entries, int logTerm) {
		
		int[] indexes2Commit = new int[entries.length];
		int term = 0;  
		try {
			for (int i = 0; i < entries.length; i++) {
				logFile.seek(fileLog.length());
				term = entries[i].getTerm() != 0 ? entries[i].getTerm() : logTerm;

				lastLogEntry = new LogEntry(lastLogEntry.getLogIndex() + 1, term,
						entries[i].getEntry(), entries[i].getClientID(), entries[i].getRequestID());
				lastLogEntry.setCommited(entries[i].isCommited());

				indexes2Commit[i] = lastLogEntry.getLogIndex();
				logFile.writeBytes(lastLogEntry.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return indexes2Commit;
	}

	/**
	 * Set a specific logEntry to committed
	 */
	public String commitLogEntry(int logEntryIndex){
		
		if (logEntryIndex > lastLogEntry.getLogIndex()) {
			return "invalid_index";
		}

		try {
			logFile.seek(0);
			for (int i = 0; i < logEntryIndex - 1; i++) {
				logFile.readLine();
			}
			long pointer = logFile.getFilePointer();
			String line = logFile.readLine();

			if (line != null && line.length() < 1) {
				return "no_content";
			}

			StringBuilder sb = new StringBuilder(line);

			int lastDelimiter = sb.lastIndexOf(LogEntry.SPLITTER);
			sb.replace(lastDelimiter + 1, sb.length(), "true ");

			logFile.seek(pointer);
			logFile.writeBytes(sb.toString());

			return new LogEntry(line).getCommand();
		} catch (IOException e) {
			System.err.println("Log file not found!");
		}
		
		return "exception";
	}

	protected void findLastLogEntry() {
		
		String line = null;
		lastLogEntry = new LogEntry();
		if (!isLogEmpty()) {
			try {
				logFile.seek(0);
				while ((line = logFile.readLine()) != null) {
					lastLogEntry = new LogEntry(line);
				}
			} catch (IOException e) {
				System.err.println("Log file not found!");
			}
		}
	}

	/**
	 * Get log index from specific entry
	 */
	protected int getLogEntryIndex (Entry entry)  {
		
		LogEntry searchEntry = null;
		int countLines=1;
		String line= null;
		try{
			logFile.seek(0);
			while ((line = logFile.readLine()) != null) {
				countLines++;
				searchEntry = new LogEntry(line);
				if(searchEntry.convertToEntry().equals(entry)){
					return countLines;
				}
			}
		} catch (IOException e) {
			System.err.println("Log file not found!");
		}
		
		return 0;
	}

	/**
	 * Get the last log Entry committed
	 */
	public LogEntry getLastCommitedLogEntry() {
		
		List<LogEntry> logs;
		logs = getAllEntriesAfterIndex(0);

		for (int i = logs.size() - 1; i >= 0; i--) {
			if (logs.get(i).isCommited()) {
				return logs.get(i);
			}
		}
		return null;
	}

	public LogEntry getLastLogEntry() {
		
		return lastLogEntry;
	}

	/**
	 * Check if log file is empty
	 */
	public boolean isLogEmpty() {
		
		try {
			return logFile.length() == 0;
		} catch (IOException e) {
			System.err.println("Log file not found!");
		}
		
		return false;
	}

	/**
	 * Check if Entry with logIndex on logTerm exists in log file
	 */
	private boolean hasEntry(int logIndex, int logTerm)  {
		
		if (isLogEmpty() && logIndex == 0 && logTerm == 0) {
			return true;
		}

		try {
			logFile.seek(0);
			String line;
			while ((line = logFile.readLine()) != null) {
				if (line.startsWith(logIndex + LogEntry.SPLITTER + logTerm)) {
					return true;
				}
			}
		} catch (IOException e) {
			System.err.println("Log file not found!");
		}
		
		return false;
	}

	/**
	 * Remove all entries after index (index excluded) until EOF
	 */
	private void removeEntriesAfterIndex(int logIndex)  {
		
		if (logIndex > 0) {
			findLastLogEntry();

			int lastLogIndex = lastLogEntry.getLogIndex();
			int size2Keep = lastLogIndex < logIndex ? lastLogIndex : logIndex;

			try {
				logFile.seek(0);
				StringBuilder sb = new StringBuilder();

				for (int i = 0; i < size2Keep; i++) {
					sb.append(logFile.readLine() + "\n");
				}

				if (sb.lastIndexOf("\n") > -1)
					sb.deleteCharAt(sb.lastIndexOf("\n"));

				logFile.setLength(0);
				logFile.writeBytes(sb.toString());
			} catch (IOException e) {
				System.err.println("Log file not found!");
			}
		}
	}

	/**
	 * Get a List of Entries with all entries after logIndex (logIndex excluded)
	 */
	public List<LogEntry> getAllEntriesAfterIndex(int logIndex)  {

		List<LogEntry> missedEntrys = new LinkedList<LogEntry>();

		String line;
		try {
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

		} catch (IOException e) {
			System.err.println("Log file not found!");
		}
		
		return null;	
	}
}