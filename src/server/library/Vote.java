package server.library;

public class Vote {
	private int term;
	private int candidateID;
	private int lastLogIndex;
	private int lastLogTerm;

	public Vote(int term, int candidateID, int lastLogIndex, int lastLogTerm) {
		super();
		this.term = term;
		this.candidateID = candidateID;
		this.lastLogIndex = lastLogIndex;
		this.lastLogTerm = lastLogTerm;
	}
	
}
