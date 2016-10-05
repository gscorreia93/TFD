package server.library;

import java.io.Serializable;

public class Response implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private int term;
	private boolean successOrVoteGranted;
	
	public Response(int term, boolean successOrVoteGranted){
		
		this.term = term;
		this.successOrVoteGranted = successOrVoteGranted;
	}

	public int getTerm() {
		return term;
	}

	public boolean isSuccessOrVoteGranted() {
		return successOrVoteGranted;
	}
}
