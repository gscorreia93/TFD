package server.library.log;

import java.util.ArrayList;
import java.util.List;

import server.library.Entry;

public enum LogHandler {
	INSTANCE;

	List<LogEntry> logs;

	private LogHandler() {
		logs = new ArrayList<>();
	}

	public void writeLog(Entry[] entries, int logTerm) {
		// Writes the log
		for (Entry e : entries) {
			logs.add(new LogEntry(getCurrentLogIndex(), logTerm, e.getEntry()));
		}
	}

	public boolean containsLogRecord(int logIndex, int logTerm) {
		return false;
	}

	public void deleteConflitingLogs(int logIndex, int logTerm) {

	}

	public LogEntry getLastLog() {
		if (!logs.isEmpty()) {
			return logs.get(logs.size() - 1);
		}
		return new LogEntry();
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
