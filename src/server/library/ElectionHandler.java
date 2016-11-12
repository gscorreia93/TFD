package server.library;

import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ElectionHandler {

	private int term = 0;
	private Server candidateServer;
	private boolean voted;
	private RAFTServers raftServers;
	private ServerThreadPool serverThreadPool;

	private ScheduledExecutorService sEs;
	Set<Integer> termVotes = new HashSet<>();

	public ElectionHandler(Server candidateServer, RAFTServers raftServers, ServerThreadPool serverThreadPool) {
		this.candidateServer = candidateServer;
		this.raftServers = raftServers;
		this.serverThreadPool = serverThreadPool;
	}

	protected void startElectionHandler() {
		resetState();
	}
	
	protected boolean hasVotedForTerm(int term) {
		if (termVotes.contains(term)) {
			return true;
		} else {
			termVotes.add(term);
			return false;
		}
	}

	protected void resetState() {
		if (sEs != null) {
			sEs.shutdownNow();
		}
		sEs = Executors.newScheduledThreadPool(2);

		// Time for election timeout
		long time2Wait = new Random().nextInt(150) + 150;

		Runnable runnable = new TimerTask() { // On election timeout
			@Override
			public void run() {
				try {

					if (candidateServer.getState() != ServerState.LEADER) {
						serverThreadPool.interruptThreads();

						List<Server> servers = raftServers.getServers();
						serverThreadPool.startThreads(servers);
							
						System.out.println("Starting election...");
						startElection();
					}

				} catch (InterruptedException e) {
					e.printStackTrace();
				} // Starts a new election

			}
		};

		sEs.scheduleWithFixedDelay(runnable, time2Wait, time2Wait, TimeUnit.MILLISECONDS);
	}

	private void startElection() throws InterruptedException {
		// First increments the term
		term++;
		// Transitions to candidate state

		if (candidateServer.getState() != ServerState.CANDIDATE) {
			candidateServer.setState(ServerState.CANDIDATE);
		}

		voted = false;

		int voteCount = 0;
		int totalVoteCount = 0;

		List<Server> servers = raftServers.getServers();

		Queue<Response> voteQueue = candidateServer.getVoteQueue();
		voteQueue.clear();

		int quorum = (servers.size() / 2) + 1;

		// Sends the vote requests
		for (Server s : servers) {
			BlockingQueue<Request> bq = s.getRequestQueue();

			if (bq.remainingCapacity() > 0) {
				bq.add(new RequestVoteRequest(term, candidateServer.getServerID(), 0, 0));
			}
		}

		// Waits for the response
		while (totalVoteCount < quorum && (voteCount < quorum)) {

			if (!voteQueue.isEmpty()) {
				Response response = voteQueue.poll();
				totalVoteCount++;

				if (response != null && response.isSuccessOrVoteGranted() && response.getTerm() == term) {
					voteCount++;
				}

				if (response != null && term < response.getTerm()) {
					//System.out.println("Rejeitado " + term + " < " + response.getTerm());
					candidateServer.setState(ServerState.FOLLOWER);
					break;
				}
			}
		}

		if (voteCount >= quorum) {
			candidateServer.setState(ServerState.LEADER);

			System.out.println("LEADER!\tTerm:["+term+"] VoteCount:["+voteCount+"]");

			Runnable runnable = new TimerTask() { // heartbeat
				@Override
				public void run() {

					List<Server> servers = raftServers.getServers();

					for (Server server : servers) {

						BlockingQueue<Request> bq = server.getRequestQueue();

						if (bq.remainingCapacity() != 0){
							AppendEntriesRequest aR = new AppendEntriesRequest(term, candidateServer.getServerID(), 0, 0, null, 0);

							if(!bq.contains(aR)){
								bq.add(aR);
							}
						}
					}
				}
			};

			// heartbeat
			sEs.scheduleWithFixedDelay(runnable, 50, 50, TimeUnit.MILLISECONDS);
		}
	}
	
	protected boolean isLeader() {
		return candidateServer.getState() == ServerState.LEADER;
	}

	protected boolean isFollower() {
		return candidateServer.getState() == ServerState.FOLLOWER;
	}

	protected int getTerm() {
		return term;
	}

	protected ServerState getState() {
		return candidateServer.getState();
	}

	protected void setServerState(ServerState state) {
		this.candidateServer.setState(state);
	}

	protected void setVoted(boolean voted) {
		this.voted = voted;
	}

	protected boolean hasVoted() {
		return voted;
	}

	protected void setTerm(int term) {
		this.term = term;
	}
}
