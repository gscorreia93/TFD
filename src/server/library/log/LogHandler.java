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
	public Response followerReplication(int term, int leaderId, int prevLogIndex, int prevLogTerm, Entry[] entries, int leaderCommit, int thisTerm) {

		Response response;

		if (thisTerm < term || hasEntry(prevLogIndex, prevLogTerm)) {
			// TODO if (thisTerm < term) update term
			System.out.println(prevLogIndex + " " + prevLogTerm);
			
			// If an existing entry conflicts with a new one (same index but 
			// different terms), delete the existing entry and all that follow it (§5.3)
			removeEntrysAfterIndex(prevLogIndex);

			LogEntry lastLog = getLastLogEntry();

			response = new Response(thisTerm, false);
			response.setLogDeprecated();
			response.setLastLogTerm(lastLog.getLogTerm());
			response.setLastLogIndex(lastLog.getLogIndex());
			return response;
		}
		
		writeLogEntries(entries, term);

		return new Response(thisTerm, true);
	}
	
	public void leaderReplication(int term, int leaderId, int prevLogIndex, int prevLogTerm, Entry[] entries, int leaderCommit, int thisTerm) {

		if (thisTerm < term || !hasEntry(prevLogIndex, prevLogTerm)) {
			// TODO if (thisTerm < term) update term

			// If an existing entry conflicts with a new one (same index but 
			// different terms), delete the existing entry and all that follow it (§5.3)
			
	
			removeEntrysAfterIndex(prevLogIndex);
		}
		
		writeLogEntries(entries, term);
	}
	
	/**
	 * Writes all entries received on log
	 */
	public void writeLogEntries(Entry[] entries, int logTerm) {
		for (int i = 0; i < entries.length; i++) {
			writeLogEntry(entries[i], logTerm);
		}
	}

	/**
	 * Write a entry to end of log file
	 */
	private void writeLogEntry(Entry entry, int logTerm) {

		int term = entry.getTerm() > 0 ? entry.getTerm() : logTerm;
		LogEntry newLogEntry = new LogEntry(getLastLogIndex(), term, entry.getEntry(), entry.getClientID());

		try {
			logFile.seek(fileLog.length());
			logFile.writeBytes(newLogEntry.toString());
		} catch (IOException e) {
			System.err.println("Log file not found!");
		}
		
	}

	/**
	 * Set a specific logEntry to committed
	 */
	public boolean commitLogEntry(int logEntryIndex){

		if (logEntryIndex > getLastLogIndex()) {
			return false;
		}

		try {
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
		} catch (IOException e) {
			System.err.println("Log file not found!");
		}

		return false;
	}

	/**
	 * Get last log index (current)
	 */
	public int getLastLogIndex()  {
		int countLines = 0;
		try {
			logFile.seek(0);
			while (logFile.readLine() != null) {
				countLines++;
			}
			return countLines;
		} catch (IOException e) {
			System.err.println("Log file not found!");
		}
		
		return 0;
		
	}
	
	/**
	 * get the last entry of logfile
	 */
	public LogEntry getLastLogEntry()  {
		try {
			logFile.seek(0);
			String line;
			while ((line = logFile.readLine()) != null) {
					return new LogEntry(line);
			}
		} catch (IOException e1) {
			System.err.println("Log file not found!");
		}
		
		return new LogEntry();
	}

	/**
	 * Get the last log Entry committed
	 */
	public int getLastCommitedLogIndex() {
		List<LogEntry> logs;
		logs = getAllEntriesAfterIndex(0);

		for (int i = logs.size() - 1; i >= 0; i--) {
			if (logs.get(i).isCommited()) {
				return logs.get(i).getLogIndex();
			}
		}
		return 0;
	}

	/**
	 * Check if log file is empty
	 */
	public boolean isLogEmpty()  {
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
	public boolean hasEntry(int logIndex, int logTerm)  {
		if (isLogEmpty() || logIndex == 0 || logTerm == 0) {
			return false;
		}

		try {
			logFile.seek(0);
			String line;
			LogEntry entry = new LogEntry();
			while ((line = logFile.readLine()) != null) {
				if (line.startsWith(logIndex + entry.getDelimiter() + logTerm)) {
					return true;
				}
			}
		} catch (IOException e) {
			System.err.println("Log file not found!");
		}
		return false;
	}

	/**
	 * Remove all entrys after index (index excluded) until EOF
	 */
	public void removeEntrysAfterIndex(int logIndex)  {
		if (logIndex > 0) {
			try {
				logFile.seek(0);
				StringBuilder sb = new StringBuilder();

				for (int i = 0; i < logIndex; i++) {
					sb.append(logFile.readLine() + "\n");
				}
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