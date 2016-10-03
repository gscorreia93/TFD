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

	public int getTerm() {
		return term;
	}

	public int getServerId() {
		return serverId;
	}

	public int getLastLogIndex() {
		return lastLogIndex;
	}

	public int getLastLogTerm() {
		return lastLogTerm;
	}
}
