package server.library.log;

import server.library.Entry;

public class LogEntry {

	public final static String SPLITTER = "|";

	private int logIndex;
	private int logTerm;
	private String command;
	private String clientID;
	private String requestID;
	private boolean commited = false;
	private int lastCommitedIndex;

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
		requestID = logParts[4];
		commited = Boolean.valueOf(logParts[5].trim());
	}

	public LogEntry(int logIndex, int logTerm, String command, String clientID, String requestID) {
		this.logIndex = logIndex;
		this.logTerm = logTerm;
		this.command = command;
		this.clientID = clientID;
		this.requestID = requestID;
	}

	public int getLogIndex() {
		return logIndex;
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
	
	public String getRequestID(){
		return requestID;
	}

	public boolean isCommited() {
		return commited;
	}
	public void setCommited(boolean commited) {
		this.commited = commited;
	}

	public String toString() {
		return logIndex + SPLITTER + logTerm + SPLITTER + command + 
				SPLITTER + clientID + SPLITTER + requestID + SPLITTER + commited + "\n";
	}
	
	public Entry convertToEntry(){
		Entry converted = new Entry(clientID, requestID, command);
		converted.setTerm(logTerm);
		converted.setCommited(commited);
		return converted;
	}

	public int getLastCommitedIndex() {
		return lastCommitedIndex;
	}

	public void setLastCommitedIndex(int lastCommitedIndex) {
		this.lastCommitedIndex = lastCommitedIndex;
	}
}
