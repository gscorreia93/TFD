package server.library.log;

public class LogEntry {

	private int logIndex;
	private int logTerm;
	private String log;

	public LogEntry(int logIndex, int logTerm, String log) {
		this.logIndex = logIndex;
		this.logTerm = logTerm;
		this.log = log;
	}

	public int getLogIndex() {
		return logIndex;
	}
	public int getLogTerm() {
		return logTerm;
	}
	public String getLog() {
		return log;
	}
}
