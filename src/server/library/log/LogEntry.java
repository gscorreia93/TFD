package server.library.log;

public class LogEntry {

	private final String SPLITTER = "_-_";

	private int logIndex;
	private int logTerm;
	private String log;
	private String clientID;
	private boolean commited = false;

	public LogEntry() {
		this.logIndex = 0;
		this.logTerm = 0;
	}
	
	public LogEntry(String logEntry) {
		String[] logParts = logEntry.split(SPLITTER);
		logIndex = Integer.parseInt(logParts[0]);
		logTerm = Integer.parseInt(logParts[1]);
		log = logParts[2];
		clientID = logParts[3];
		commited = logParts[4].equals("true");
	}

	public LogEntry(int logIndex, int logTerm, String log, String clientID) {
		this.logIndex = logIndex;
		this.logTerm = logTerm;
		this.log = log;
		this.clientID = clientID;
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
	public String getClientID() {
		return clientID;
	}

	public boolean isCommited() {
		return commited;
	}
	public void setCommited(boolean commited) {
		this.commited = commited;
	}

	public String toString() {
		return logIndex + SPLITTER + logTerm + SPLITTER + log + SPLITTER + clientID + SPLITTER + commited;
	}

	public byte[] writeln() {
		return (toString() + "\n").getBytes();
	}
}
