package server.library.log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import server.library.Entry;

/**
 * Class that handles the logs.
 */
public class LogHandler {

	private File logFile;
	private List<LogEntry> logs;

	public LogHandler(String filename) {
		logs = new ArrayList<>();

		try {
			readFile(filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readFile(String filename) throws IOException {
		logFile = new File(filename);
		if (!logFile.exists()) {
			logFile.createNewFile();
		}
	}

	/**
	 * Writes a log entry
	 * @return the log entry index
	 */
	public int writeLogEntry(Entry[] entries, int logTerm) {
		// Writes the log
		for (Entry e : entries) {
			logs.add(new LogEntry(getCurrentLogIndex(), logTerm, e.getEntry(), e.getClientID()));
		}
		return logs.size();
	}

	/**
	 * Commits a log entry
	 */
	public String commitLogEntry(int commitEntryIndex) {
		String commitedLog = null;

		if (logs.size() == commitEntryIndex) {
			logs.get(commitEntryIndex - 1).setCommited(true);
			commitedLog = logs.get(commitEntryIndex - 1).getLog();
		}
		return commitedLog;
	}

	public boolean containsLogRecord(int logIndex, int logTerm) {
		if (logs.isEmpty() && logIndex == 0)
			return true;

		for (int i = logs.size() - 1; i >= 0; i--) {
			if (logs.get(i).getLogIndex() == logIndex && logs.get(i).getLogTerm() == logTerm) {
				return true;
			}
		}

		return false;
	}

	public void deleteConflitingLogs(int logIndex, int logTerm) {
		if (logs.size() >= logIndex && logs.get(logIndex - 1).getLogTerm() != logTerm) {
			logs.remove(logIndex - 1);
			deleteConflitingLogs(logIndex - 1, logTerm);
		}
	}

	public LogEntry getLastLog() {
		return getLogAtIndex(logs.size() - 1);
	}

	public LogEntry getLogAtIndex(int logIndex) {
		if (!logs.isEmpty()) {
			return logs.get(logIndex);
		}
		return new LogEntry();
	}

	/**
	 * Gets the logs since a given index to replicate
	 * to follower servers that are not up to date.
	 * @return
	 */
	public Entry[] getLogsSinceIndex(int logIndex) {
		List<LogEntry> tempLogs = new ArrayList<>();

		for (int i = 0; i < logs.size(); i++) {
			if (i == logIndex) {
				tempLogs.add(logs.get(i));
			}
		}

		Entry[] logs2Return = new Entry[tempLogs.size()];
		for (int i = 0; i < tempLogs.size(); i++) {
			logs2Return[i] = new Entry(tempLogs.get(i).getClientID(), null, tempLogs.get(i).getLog());
		}
		return logs2Return;
	}

	public int getLastCommitedLogIndex() {
		for (int i = logs.size() -1; i >= 0; i--) {
			if (logs.get(i).isCommited()) {
				return logs.get(i).getLogIndex();
			}
		}
		return 0;
	}

	private int getCurrentLogIndex() {
		return logs.size();
	}
}
