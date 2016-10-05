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

	public ElectionHandler(Server candidateServer) {

		this.candidateServer = candidateServer;
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

	protected void setVoted() {

		this.voted = true;
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
		long time2Wait = new Random().nextInt(2000) + 2000;

		// election timeout
		timer.schedule(new TimerTask() { // On election timeout
			@Override
			public void run() {
				try {

					System.out.println("I AM " + candidateServer.getState());
					if (candidateServer.getState() != ServerState.LEADER) {
						System.out.println("starting election");
						startElection();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} // Starts a new election

			}
		}, time2Wait);
	}

	protected void startElection() throws InterruptedException {
		// First increments the term
		term++;
		// Transitions to candidate state
		candidateServer.setState(ServerState.CANDIDATE);

		int voteCount = 0;

		List<Server> servers = RAFTServers.INSTANCE.getServers();

		Queue<Response> responseQueue = candidateServer.getResponseQueue();

		int quorum = (servers.size() / 2) + 1;
		
		boolean requested = false;

		while (voteCount < quorum && !hasLeader) {
			
			System.out.println("RequestForVote");
			
			if (!requested) {
				for (Server server : servers) {

					BlockingQueue<Request> bq = server.getRequestQueue();
					
					bq.add(new RequestVoteRequest(term, candidateServer.getServerID(), 0, 0));
				}
				requested = true;
			}

			if (!responseQueue.isEmpty()) {
				
				Response response = responseQueue.poll();
								
				if (response.isSuccessOrVoteGranted()) {
					voteCount++;
				}
				
				if(term < response.getTerm()){
					candidateServer.setState(ServerState.FOLLOWER);
					break;					
				}
			}

			Thread.sleep(1000);

		}

		if (!hasLeader) {
			candidateServer.setState(ServerState.LEADER);

			System.out.println("SOU LIDER!!!!");

			// heartbeat
			timer.schedule(new TimerTask() { // heartbeat
				@Override
				public void run() {

					System.out.println("Beep!");

					List<Server> servers = RAFTServers.INSTANCE.getServers();

					for (Server server : servers) {

						BlockingQueue<Request> bq = server.getRequestQueue();

						bq.add(new AppendEntriesRequest(term, candidateServer.getServerID(), 0, 0, null, 0));
					}
				}
			}, 500, 500);
		}
	}
}
