package server.library;

public class RequestVoteRequest extends Request {

	protected RequestVoteRequest(int term, int serverId, int lastLogIndex, int lastLogTerm){
		super(term, serverId, lastLogIndex, lastLogTerm);
	}
}
