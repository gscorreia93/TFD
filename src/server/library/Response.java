package server.library;

import java.io.Serializable;

public class Response implements Serializable{

	private static final long serialVersionUID = 1L;
	private final int LOG_DEPRECATED = 1;

	private int term;
	private int leaderID;
	private int denyCause;
	private int lastLogTerm;
	private int lastLogIndex;
	private String requestID;
	private String response;
	private boolean successOrVoteGranted;
	private int serverID;

	public Response(int term, boolean successOrVoteGranted) {
		
		this.term = term;
		this.successOrVoteGranted = successOrVoteGranted;
	}

	protected Response(int term, String response) {
		
		this.term = term;
		this.response = response;
		this.successOrVoteGranted = true;
	}
	
	public int getTerm() {
		
		return term;
	}

	public boolean isSuccessOrVoteGranted() {
		
		return successOrVoteGranted;
	}

	public String getResponse() {
		
		return response;
	}

	protected void setLeaderID (int leaderId){
		
		leaderID = leaderId;
	}
	
	public int getLeaderID(){
		
		return leaderID;
	}

	protected String getRequestID(){
		
		return requestID;
	}
	
	public void setRequestID(String requestID) {
		
		this.requestID = requestID;
	}

	protected boolean isLogDeprecated() {
		
		return this.denyCause == LOG_DEPRECATED;
	}
	
	public void setLogDeprecated() {
		
		this.denyCause = LOG_DEPRECATED;
	}

	/**
	 * Resets the log deprecated and requestID
	 */
	protected void resetLogDeprecated() {
		
		this.denyCause = 0;
		this.requestID = "null";
	}

	protected int getLastLogTerm() {
		
		return lastLogTerm;
	}
	
	public void setLastLogTerm(int lastLogTerm) {
		
		this.lastLogTerm = lastLogTerm;
	}

	protected int getLastLogIndex() {
		
		return lastLogIndex;
	}
	
	public void setLastLogIndex(int lastLogIndex) {
		
		this.lastLogIndex = lastLogIndex;
	}
	
	protected int getServerID() {
		
		return serverID;
	}

	protected void setServerID(int serverID) {
		
		this.serverID = serverID;
	}

	@Override
	public String toString() {
		
		return "leaderID: " + leaderID
				+ ", term: " + term
				+ ", successOrVoteGranted: " + successOrVoteGranted
				+ (successOrVoteGranted ? "" : ", denyCause: " + (denyCause == LOG_DEPRECATED ? "LOG_DEPRECATED" : "?"));
	}
}
