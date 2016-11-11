package server.library.log;

import java.util.concurrent.BlockingQueue;

import server.library.AppendEntriesRequest;
import server.library.Entry;
import server.library.Request;
import server.library.Response;
import server.library.Server;
import server.library.ServerHandler;

/**
 * Thread that submits a log to a remote server and
 * keeps running until the log is submitted and the
 * remote server logs are synchronized with the leader logs 
 */
public class LogSubmitThread extends Thread {

	private Server s;
	private int term;
	private int serverId;
	private Entry[] entries;
	private int leaderCommit;
	private LogHandler lh;
	
	private int lastLogIndex;
	private int lastLogTerm;
	
	private boolean submitted = false;
	private volatile boolean allowCommit = false;

	public LogSubmitThread(Server s, int term, int serverId,
			int lastLogIndex, int lastLogTerm, Entry[] entries, int leaderCommit, LogHandler lh) {

		this.s = s;
		this.term = term;
		this.serverId = serverId;
		this.entries = entries;
		this.leaderCommit = leaderCommit;
		this.lh = lh;
		
		this.lastLogIndex = lastLogIndex;
		this.lastLogTerm = lastLogTerm;

		BlockingQueue<Request> bq = s.getRequestQueue();

		if (bq.remainingCapacity() > 0) {
			// Replicate log to the server
			s.getRequestQueue().add(new AppendEntriesRequest(term, serverId,
					lastLogIndex, lastLogTerm, entries, leaderCommit));
		}
	}

	@Override
	public void run() {
		while (!submitted) {

			if (!s.getResponseQueue().isEmpty()) { // Gets the server result
				Response response = s.getResponseQueue().poll();

				if (response != null) {

					if (response.isSuccessOrVoteGranted()) {
						submitted = true;

					} else if (response.isLogDeprecated()) {
						entries = lh.getEntriesSinceIndex(response.getLastLogIndex());

						// Sends all the logs from the received last Index
						s.getRequestQueue().add(new AppendEntriesRequest(term, serverId,
								response.getLastLogIndex(), response.getLastLogTerm(), entries, leaderCommit));
					}
				}
			}

			try {
				// Waits to see again if there is a new result from the server
				Thread.sleep(300);
			} catch (InterruptedException e) {}
		} // eof while
System.out.println("Log submitted to server " + s.getPort());

		do {
			// Nothing while it can't commit
			try {
				// Waits to see again if it is allowed to commit
				Thread.sleep(300);
			} catch (InterruptedException e) {}
		} while (!allowCommit);

		// When the
		BlockingQueue<Request> bq = s.getRequestQueue();

		if (bq.remainingCapacity() > 0) {
			s.getRequestQueue().add(new AppendEntriesRequest(ServerHandler.COMMIT_LOG, serverId,
					lastLogIndex, lastLogTerm, entries, leaderCommit));
		}
	}

	/**
	 * @return true if the log was submitted
	 * and the remote server is synchronized
	 * with the leader
	 */
	public boolean isSubmitted() {
		return submitted;
	}

	/**
	 * Tells the thread that it can commit the log
	 * and receives the index to commit
	 */
	public void allowCommit(int leaderCommit) {
		this.leaderCommit = leaderCommit;
		this.allowCommit = true;
	}

	public Server getServer() {
		return s;
	}
}
