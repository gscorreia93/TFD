package server.library;

import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;

public class ElectionHandler {

	private Timer timer;

	private int term = 0;
	private Server candidateServer;
	private boolean voted;
	private boolean hasLeader;
	private RAFTServers raftServers;

	public ElectionHandler(Server candidateServer, RAFTServers raftServers) {

		this.candidateServer = candidateServer;
		this.raftServers = raftServers;
	}

	protected void startElectionHandler() {
		resetState();
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

	protected void setHasLeader() {

		this.hasLeader = true;
	}

	protected void resetTimer() {

		timer.cancel();
	}

	protected void resetState() {
		timer = new Timer();

		// Time for election timeout
		long time2Wait = new Random().nextInt(150) + 150;
		
		// election timeout
		timer.schedule(new TimerTask() { // On election timeout
			@Override
			public void run() {
				try {

					if (candidateServer.getState() != ServerState.LEADER) {
						System.out.println("starting election");
						startElection();
					}
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				} // Starts a new election

			}
		}, time2Wait, time2Wait);
	}

	protected void startElection() throws InterruptedException {
		
		// First increments the term
		term++;
		// Transitions to candidate state
		
		if(candidateServer.getState() != ServerState.CANDIDATE){
			candidateServer.setState(ServerState.CANDIDATE);
		}
		
		hasLeader = false;
		voted = false;

		int voteCount = 0;
		int totalVoteCount = 0;

		List<Server> servers = raftServers.getServers();

		Queue<Response> responseQueue = candidateServer.getResponseQueue();
		responseQueue.clear();
		
		int quorum = (servers.size() / 2) + 1;
		
		boolean requested = false;

		while (totalVoteCount < quorum && (voteCount < quorum && !hasLeader)) {
			
			if (!requested) {
				for (Server server : servers) {

					BlockingQueue<Request> bq = server.getRequestQueue();
					
					if(bq.remainingCapacity() != 0){
						bq.add(new RequestVoteRequest(term, candidateServer.getServerID(), 0, 0));
					}
				}
				requested = true;
			}

			if (!responseQueue.isEmpty()) {
				Response response = responseQueue.poll();
				totalVoteCount++;
								
				if (response != null && response.isSuccessOrVoteGranted() && response.getTerm() == term) {
					voteCount++;
				}
				
				if(response != null && term < response.getTerm()){
					candidateServer.setState(ServerState.FOLLOWER);
					break;					
				}
			}
		}
		
		
		if (!hasLeader && voteCount >= quorum) {
			candidateServer.setState(ServerState.LEADER);

			System.out.println("SOU LIDER!!!! no term:" + term + " COM VOTOS COUNT: " + voteCount);

			// heartbeat
			timer.schedule(new TimerTask() { // heartbeat
				@Override
				public void run() {

					//System.out.println("Beep!");

					List<Server> servers = raftServers.getServers();

					for (Server server : servers) {

						BlockingQueue<Request> bq = server.getRequestQueue();

						if (bq.remainingCapacity() != 0){
							bq.add(new AppendEntriesRequest(term, candidateServer.getServerID(), 0, 0, null, 0));
						}
					}
				}
			}, 50, 50);
		}
	}
}
