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
	private boolean successOrVoteGranted;

	public Response(int term, boolean successOrVoteGranted) {
		this.term = term;
		this.successOrVoteGranted = successOrVoteGranted;
	}

	public int getTerm() {
		return term;
	}

	public boolean isSuccessOrVoteGranted() {
		return successOrVoteGranted;
	}

	public void setLeaderID (int leaderId){
		leaderID = leaderId;
	}

	public int getLeaderID(){
		return leaderID;
	}

	public boolean isLogDeprecated() {
		return denyCause == LOG_DEPRECATED;
	}
	public void setLogDeprecated() {
		denyCause = LOG_DEPRECATED;
	}

	public int getLastLogTerm() {
		return lastLogTerm;
	}
	public void setLastLogTerm(int lastLogTerm) {
		this.lastLogTerm = lastLogTerm;
	}

	public int getLastLogIndex() {
		return lastLogIndex;
	}
	public void setLastLogIndex(int lastLogIndex) {
		this.lastLogIndex = lastLogIndex;
	}

	public String toString() {
		return "leaderID: " + leaderID
				+ ", term: " + term
				+ ", successOrVoteGranted: " + successOrVoteGranted
				+ (successOrVoteGranted ? "" : ", denyCause: " + (denyCause == LOG_DEPRECATED ? "LOG_DEPRECATED" : "?"));
	}
}
