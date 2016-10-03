package server.library;

public class AppendEntriesRequest extends Request {

	private Entry[] entries;
	private int leaderCommit;
	
	protected AppendEntriesRequest(int term, int serverId, int lastLogIndex, int lastLogTerm, Entry[] entries, int leaderCommit){
		super(term, serverId, lastLogIndex, lastLogTerm);
		
		this.entries = entries;
		this.leaderCommit = leaderCommit;
	}

	public Entry[] getEntries() {
		return entries;
	}

	public int getLeaderCommit() {
		return leaderCommit;
	}
}
