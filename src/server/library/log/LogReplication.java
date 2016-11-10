package server.library.log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import server.library.AppendEntriesRequest;
import server.library.Entry;
import server.library.Request;
import server.library.Response;
import server.library.Server;
import server.library.ServerHandler;

public class LogReplication {

	private Server server;
	private LogHandler lh;
	private List<Server> servers;

	public LogReplication(Server server, List<Server> servers, LogHandler lh) {
		this.server = server;
		this.servers = servers;
		this.lh = lh;
	}

	/**
	 * Synchronized method to start the leader logs replication.
	 * 
	 * @param entries
	 * @param term
	 * @return true when at least half the servers have written the logs.
	 */
	public synchronized Response leaderReplication(Entry[] entries, int term) {
System.out.println(entries[0].getClientID() + " says '" + entries[0].getEntry() + "'");

		// To store the servers that are valid to commit the entry
		List<Server> commitServers = new ArrayList<>();
		// To store the threads submitting the logs
		List<LogSubmitThread> logSubmits = new ArrayList<>();

		LogEntry lastLog = lh.getLastLog();
		int leaderCommit = lh.getLastCommitedLogIndex();

		// Used to commit this log latter
		int currentIndex = lh.writeLogEntry(entries, term);


		for (Server s : servers) {
			if (s.equals(server)) {
				continue; // Doesn't need to create a thread to the leader
			}

			// Replicate log to the other servers
			LogSubmitThread l = new LogSubmitThread(s, term, server.getServerID(),
					lastLog.getLogIndex(), lastLog.getLogTerm(), entries, leaderCommit, lh);

			logSubmits.add(l);
			// Orders the thread to submit the log to the remote server
			l.start();
		}


		int responsesCount = 0;
		int quorum = (servers.size() / 2); // Only half the servers need to respond

		while (responsesCount < quorum) {
			for (int i = logSubmits.size() - 1; i >= 0; i--) {

				if (logSubmits.get(i).isSubmitted()) {
					commitServers.add(logSubmits.get(i).getServer());
					// When the thread has ended it is removed from the queue
					logSubmits.remove(i);
					responsesCount++;
				}
			}

			try {
				// Waits to see again if there is a new result from a server
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} // eof while



		// First commits in the leader
		commitLog(currentIndex, term);

		// Then sends request for all other servers to commit
		for (Server s : commitServers) {
			BlockingQueue<Request> bq = s.getRequestQueue();

			if (bq.remainingCapacity() > 0) {
				s.getRequestQueue().add(new AppendEntriesRequest(ServerHandler.COMMIT_LOG, server.getServerID(),
						lastLog.getLogIndex(), lastLog.getLogTerm(), entries, currentIndex));
			}
		}
		// There's no need to wait for the commit results to reply to the client

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
			int prevLogIndex, int prevLogTerm, Entry[] entries, int leaderCommit, int thisTerm) {

		Response response;

		if (thisTerm < term) {
			response = new Response(thisTerm, false);
			response.setTermRejected();
			return response;

		} else if (!lh.containsLogRecord(prevLogIndex, prevLogTerm)) {

			// If an existing entry conflicts with a new one (same index but 
			// different terms), delete the existing entry and all that follow it (§5.3)
			lh.deleteConflitingLogs(prevLogIndex + 1, term);

			response = new Response(thisTerm, false);
			response.setLogDeprecated();
			response.setLastLogIndex(lh.getLastLog().getLogIndex());
			return response;
		}

		// Writes log
		lh.writeLogEntry(entries, term);

		return new Response(thisTerm, true);
	}

	/**
	 * Only commits a log on the leaderCommit index, there are no validations.
	 */
	public Response commitLog(int leaderCommit, int thisTerm) {
		String commitedLog = lh.commitLogEntry(leaderCommit);

System.out.println(leaderCommit + ": Commiting '" + commitedLog + "' on " + server.getPort() + " as a " + server.getState() + " at " + leaderCommit);

		return new Response(thisTerm, true);
	}
}
