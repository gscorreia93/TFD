package server.library;

import java.util.Arrays;

public class AppendEntriesRequest extends Request {

	private Entry[] entries;
	private int leaderCommit;
	
	protected AppendEntriesRequest(int term, int serverId, int lastLogIndex, int lastLogTerm, Entry[] entries, int leaderCommit) {
		super(term, serverId, lastLogIndex, lastLogTerm);
		
		this.entries = entries;
		this.leaderCommit = leaderCommit;
	}

	protected Entry[] getEntries() {
		
		return entries;
	}

	protected int getLeaderCommit() {
		
		return leaderCommit;
	}

	@Override
	public int hashCode() {
		
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(entries);
		result = prime * result + leaderCommit;
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		AppendEntriesRequest other = (AppendEntriesRequest) obj;
		if (!Arrays.equals(entries, other.entries))
			return false;
		if (leaderCommit != other.leaderCommit)
			return false;
		
		return true;
	}
}
