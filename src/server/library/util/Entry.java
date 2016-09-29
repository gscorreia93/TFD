package server.library.util;

public class Entry {
	private int term;
	private int candidateID;
	private int lastLogIndex;
	private int lastLogTerm;
	private String[] entries;
	private int leaderCommit;

	public Entry(int term, int candidateID, int lastLogIndex, int lastLogTerm,
			String[] entries, int leaderCommit) {
		super();
		this.term = term;
		this.candidateID = candidateID;
		this.lastLogIndex = lastLogIndex;
		this.lastLogTerm = lastLogTerm;
		this.entries = entries;
		this.leaderCommit = leaderCommit;
	}

}
