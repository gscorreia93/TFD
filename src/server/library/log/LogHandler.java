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
	
	private int getCurrentLogIndex() {
		return logs.size();
	}
}
