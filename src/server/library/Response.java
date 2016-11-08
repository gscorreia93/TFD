package server.library;

import java.io.Serializable;

public class Response implements Serializable{

	private static final long serialVersionUID = 1L;

	private final int TERM_REJECTED = 1;
	private final int LOG_DEPRECATED = 2;

	private int term;
	private int leaderID;
	private int denyCause;
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
	
	public boolean isTermRejected() {
		return denyCause == TERM_REJECTED;
	}
	public void setTermRejected() {
		denyCause = TERM_REJECTED;
	}

	public boolean isLogDeprecated() {
		return denyCause == LOG_DEPRECATED;
	}
	public void setLogDeprecated() {
		denyCause = LOG_DEPRECATED;
	}

	public int getLastLogIndex() {
		return lastLogIndex;
	}
	public void setLastLogIndex(int lastLogIndex) {
		this.lastLogIndex = lastLogIndex;
	}
}
