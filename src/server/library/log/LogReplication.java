package server.library.log;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import server.library.AppendEntriesRequest;
import server.library.Entry;
import server.library.Request;
import server.library.Response;
import server.library.Server;
import server.library.ServerHandler;
import server.library.ServerState;

public class LogReplication {

	private Server server;
	private List<Server> servers;

	public LogReplication(Server server, List<Server> servers) {
		this.server = server;
		this.servers = servers;
	}

	/**
	 * Synchronized method to start the leader logs replication.
	 * 
	 * @param entries
	 * @param term
	 * @return true
	 */
	public synchronized Response leaderReplication(Entry[] entries, int term) {
System.out.println("leaderReplication: " + ServerState.LEADER + " & term: " + term);

		if (server.getState() != ServerState.LEADER) {
			// Sends the leader address back to the client

		} else {
			// Writes log
			LogHandler lh = LogHandler.INSTANCE;

			lh.writeLog(entries, term);

			
			LogEntry lastLog = lh.getLastLog();
			int leaderCommit = 10; // lh.getLastCommitedLogIndex();

			for (Server s : servers) {
				BlockingQueue<Request> bq = s.getRequestQueue();

				if (bq.remainingCapacity() > 0) {
					// Replicate log to other servers
					s.getRequestQueue().add(new AppendEntriesRequest(term, server.getServerID(),
							lastLog.getLogIndex(), lastLog.getLogTerm(), entries, leaderCommit));
				}
			}

			int responsesCount = 0;
			int quorum = (servers.size() / 2); // Only half the servers need to respond

			while (responsesCount < quorum) {
				for (Server s : servers) {

					if (!s.getResponseQueue().isEmpty()) { // Gets the server result
						Response response = s.getResponseQueue().poll();

						if (response != null && response.isSuccessOrVoteGranted()) {
							responsesCount++;
						}
					}
				}

				try {
					// Waits to see again if there is a new result from a server
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			} // eof while

			// Sends request for all servers to commit
			for (Server s : servers) {
				BlockingQueue<Request> bq = s.getRequestQueue();

				if (bq.remainingCapacity() > 0) {
					s.getRequestQueue().add(new AppendEntriesRequest(ServerHandler.COMMIT_LOG, server.getServerID(),
							lastLog.getLogIndex(), lastLog.getLogTerm(), entries, leaderCommit));
				}
			}

			// TODO Needs to wait for the commit results to reply to the client

		} // eof if

		return new Response(term, true);
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
	public Response followerReplication(int term, int leaderId,
			int prevLogIndex, int prevLogTerm, Entry[] entries, int leaderCommit, int currentTerm) {
System.out.println("Replicated log with term " + term + " and currentTerm " + currentTerm);

		LogHandler lh = LogHandler.INSTANCE;

		Response response;

		if (term < currentTerm) {
			response = new Response(currentTerm, false);
			response.setTermRejected();
			// return response;

		} else if (!lh.containsLogRecord(prevLogIndex, prevLogTerm)) {
			response = new Response(currentTerm, false);
			response.setLogDeprecated();
			// return response;
		}

		// If an existing entry conflicts with a new one (same index but 
		// different terms), delete the existing entry and all that follow it (§5.3)
		lh.deleteConflitingLogs(prevLogIndex, prevLogTerm);

		// Writes log
		lh.writeLog(entries, term);

		return new Response(currentTerm, true);
	}

	/**
	 * Only commits a log, there are no validations.
	 */
	public void commitLog(int term,
			int prevLogIndex, int prevLogTerm, Entry[] entries, int leaderCommit) {
		
		// TODO Implement the log commit
		System.out.println("Commiting on server " + server.getPort() + " as a " + server.getState());
	}
}
