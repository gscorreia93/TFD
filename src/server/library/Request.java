package server.library;

public abstract class Request {
	
	private int term;
	private int serverId;
	private int lastLogIndex;
	private int lastLogTerm;
	
	protected Request(int term, int serverId, int lastLogIndex, int lastLogTerm){
		
		this.term = term;
		this.serverId = serverId;
		this.lastLogIndex = lastLogIndex;
		this.lastLogTerm = lastLogTerm;
	}

	protected int getTerm() {
		
		return term;
	}

	protected int getServerId() {
		
		return serverId;
	}

	protected int getLastLogIndex() {
		
		return lastLogIndex;
	}

	protected int getLastLogTerm() {
		
		return lastLogTerm;
	}
	
	@Override
	public int hashCode() {
		
		final int prime = 31;
		int result = 1;
		result = prime * result + lastLogIndex;
		result = prime * result + lastLogTerm;
		result = prime * result + serverId;
		result = prime * result + term;
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Request other = (Request) obj;
		if (lastLogIndex != other.lastLogIndex)
			return false;
		if (lastLogTerm != other.lastLogTerm)
			return false;
		if (serverId != other.serverId)
			return false;
		if (term != other.term)
			return false;
		return true;
	}
}
