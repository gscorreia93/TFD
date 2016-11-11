package server.library.log;

import java.util.ArrayList;
import java.util.List;

import server.library.Entry;
import server.library.Response;
import server.library.Server;

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
					responsesCount++;
				}
			}

			try {
				// Waits to see again if there is a new result from a server
				Thread.sleep(300);
			} catch (InterruptedException e) {}

		} // eof while


		// First commits in the leader
		commitLog(currentIndex, term);

		// Then sends request for all other servers threads to commit
		for (int i = logSubmits.size() - 1; i >= 0; i--) {
			logSubmits.get(i).allowCommit(currentIndex);
			// When the thread has ended it is removed from the queue
			logSubmits.remove(i);
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

		if (thisTerm < term || !lh.containsLogRecord(prevLogIndex, prevLogTerm)) {
			// TODO if (thisTerm < term) update term

			// If an existing entry conflicts with a new one (same index but 
			// different terms), delete the existing entry and all that follow it (§5.3)
			lh.deleteConflitingLogs(prevLogIndex);

			LogEntry lastLog = lh.getLastLog();

			response = new Response(thisTerm, false);
			response.setLogDeprecated();
			response.setLastLogTerm(lastLog.getLogTerm());
			response.setLastLogIndex(lastLog.getLogIndex());
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
