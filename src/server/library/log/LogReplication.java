package server.library.log;

import java.util.List;

import server.library.Entry;
import server.library.RAFTServers;
import server.library.Server;
import server.library.ServerState;

public class LogReplication {

	private Server server;
	
	public LogReplication(Server server) {
		this.server = server;
	}
	
	/**
	 * Synchronized method to start the leader logs replication.
	 * 
	 * @param entries
	 * @param term
	 * @return true
	 */
	public synchronized boolean leaderReplication(Entry[] entries, int term) {
		if (server.getState() != ServerState.LEADER) {
			// Sends the leader address back to the client
		
		} else {
			// Writes log
			LogHandler.INSTANCE.writeLog(entries, term);
			
			// Send log to others
			List<Server> servers = RAFTServers.INSTANCE.getServers();
			
			for (Server s : servers) {
				s.getRequestQueue().add(null);
				
			}
			
			
			for (Server s : servers) {
				s.getResponseQueue().poll();
				
			}
			
		}
		return true;
	}
	
	/**
	 * Follower writes a client log received
	 * from the leader.
	 * 
	 * If an existing entry conflicts with a new one (same index
	 * 	but different terms), delete the existing entry and all
	 * 	that follow it (§5.3)
	 * Append any new entries not already in the log
	 * If leaderCommit > commitIndex, set commitIndex =
	 * 	min(leaderCommit, index of last new entry)
	 * 
	 * @return
	 * 1. false if term < currentTerm (§5.1)
	 * 2. false if log doesn’t contain an entry at prevLogIndex
	 * 		whose term matches prevLogTerm (§5.3)
	 */
	public boolean followerReplication(int term, int leaderId,
			int prevLogIndex, int prevLogTerm, Entry[] entries, int leaderCommit, int currentTerm) {

		LogHandler lh = LogHandler.INSTANCE;
		
		if (term < currentTerm || !lh.containsLogRecord(prevLogIndex, prevLogTerm)) {
			return false;
		}
		
		lh.deleteConflitingLogs(prevLogIndex, prevLogTerm);
		
		// Writes log
		lh.writeLog(entries, term);
		
		return true;
	}
}
