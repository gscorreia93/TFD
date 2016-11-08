package server.library.log;

import java.util.concurrent.BlockingQueue;

import server.library.AppendEntriesRequest;
import server.library.Entry;
import server.library.Request;
import server.library.Response;
import server.library.Server;

/**
 * Thread that submits a log to a remote server and
 * keeps running until the log is submitted and the
 * remote server logs are synchronized with the leader logs 
 */
public class LogSubmitThread extends Thread {

	private Server s;
	private int term;
	private int serverId;
	private int lastLogIndex;
	private int lastLogTerm;
	private Entry[] entries;
	private int leaderCommit;
	private LogHandler lh;
	
	private boolean submitted = false;

	public LogSubmitThread(Server s, int term, int serverId,
			int lastLogIndex, int lastLogTerm, Entry[] entries, int leaderCommit, LogHandler lh) {

		this.s = s;
		this.term = term;
		this.serverId = serverId;
		this.lastLogIndex = lastLogIndex;
		this.lastLogTerm = lastLogTerm;
		this.entries = entries;
		this.leaderCommit = leaderCommit;
		this.lh = lh;

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
System.out.println(s.getPort() + ": " + response);

					if (response.isSuccessOrVoteGranted()) {
						submitted = true;

					} else if (response.isLogDeprecated()) {
System.out.println(s.getPort() + " deprecated; last index " + response.getLastLogIndex());

						entries = lh.getLogsSinceIndex(response.getLastLogIndex());

						// Sends all the logs from the received last Index
						s.getRequestQueue().add(new AppendEntriesRequest(term, serverId,
								lastLogIndex, lastLogTerm, entries, leaderCommit));

					} else if (response.isTermRejected()) {
System.out.println(s.getPort() + " term rejected; current term " + response.getTerm());

					}
				}
			}

			try {
				// Waits to see again if there is a new result from the server
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} // eof while
		
System.out.println("Log submitted to server " + s.getPort());
	}

	/**
	 * @return true if the log was submitted
	 * and the remote server is synchronized
	 * with the leader
	 */
	public boolean isSubmitted() {
		return submitted;
	}

	public Server getServer() {
		return s;
	}
}
