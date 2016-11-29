package server.library.log;

import server.library.Entry;

public class LogEntry {

	private final String SPLITTER = "|";

	private int logIndex;
	private int logTerm;
	private String command;
	private String clientID;
	private boolean commited = false;

	public LogEntry() {
		this.logIndex = 0;
		this.logTerm = 0;
	}
	
	public LogEntry(String logEntry) {
		String[] logParts = logEntry.split("\\"+SPLITTER);
		logIndex = Integer.parseInt(logParts[0]);
		logTerm = Integer.parseInt(logParts[1]);
		command = logParts[2];
		clientID = logParts[3];
		commited = Boolean.valueOf(logParts[4].trim());
	}

	public LogEntry(int logIndex, int logTerm, String command, String clientID) {
		this.logIndex = logIndex;
		this.logTerm = logTerm;
		this.command = command;
		this.clientID = clientID;
	}

	public int getLogIndex() {
		return logIndex;
	}
	
	public String getDelimiter(){
		return SPLITTER;
	}
	public int getLogTerm() {
		return logTerm;
	}
	public String getCommand() {
		return command;
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
		return logIndex + SPLITTER + logTerm + SPLITTER + command + SPLITTER + clientID + SPLITTER + commited;
	}
	
	public Entry convertToEntry(){
		Entry converted = new Entry(clientID, null, command);
		converted.setTerm(logTerm);
		converted.setCommited(commited);
		return converted;
	}

//	public byte[] writeln() {
//		return (toString() + "\n").getBytes();
//	}
}
