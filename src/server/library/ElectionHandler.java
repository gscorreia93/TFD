package server.library;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class ElectionHandler {

	private Timer timer;

	private int term = 0;
	private ServerState state = ServerState.FOLLOWER;

	protected void startElectionHandler() {
		resetState();
	}

	protected boolean isLeader() {
		return state == ServerState.LEADER;
	}

	protected boolean isFollower() {
		return state == ServerState.FOLLOWER;
	}

	protected int getTerm() {

		return term;
	}
	
	protected ServerState getState(){
		
		return state;
	}

	protected void resetState() {
		timer = new Timer();

		// Time for election timeout
		long time2Wait = new Random().nextInt(2000) + 2000;

		timer.schedule(new TimerTask() { // On election timeout
			@Override
			public void run() {
				try {
					startElection();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // Starts a new election

				// Resets the state again
				// ---------------------resetState();
			}
		}, time2Wait);
	}
	
	protected void interruptState(){
		
		if(timer != null){
			timer.cancel();
			timer.purge();
		}
	}

	protected void startElection() throws InterruptedException {
		// First increments the term
		term++;
		// Transitions to candidate state
		state = ServerState.CANDIDATE;

		int voteCount = 0;

		List<Server> servers = RAFTServers.INSTANCE.getServers();

		Queue<Boolean> responseQueue = new PriorityQueue<Boolean>();

		int quorum = (servers.size() / 2) + 1;

		while (voteCount < quorum) {
			System.out.println("RequestForVote");
			voteCount = 0;
			for (Server server : servers) {
				CommThread thread = new CommThread(server, responseQueue);
				thread.start();
			}

			int responseCounter = 0;

			while (responseCounter < servers.size()) {
				if (!responseQueue.isEmpty()) {
					if (responseQueue.poll()) {
						voteCount++;
					}
					
					responseCounter++;
				}
				Thread.sleep(1000);
			}

			System.out.println("Vote Count: " + voteCount);
		}
		
		state = ServerState.LEADER;
		System.out.println("SOU LIDER!!!!");
		
		for (Server server : servers) {
			CommThread thread = new CommThread(server, responseQueue);
			thread.start();
		}
	}

	private class CommThread extends Thread {

		private Server server;
		private Queue<Boolean> queue;

		private CommThread(Server server, Queue<Boolean> queue) {

			this.server = server;
			this.queue = queue;
		}

		@Override
		public void run() {

			Registry registry;
			try {
				
				if(state == ServerState.CANDIDATE){
					registry = LocateRegistry.getRegistry(server.getPort());
					RemoteMethods stub = (RemoteMethods) registry.lookup("ServerHandler");

					boolean response = stub.requestVote(term, 1, 0, 0);

					queue.add(response);
					
				}else if(state == ServerState.LEADER){
					
					registry = LocateRegistry.getRegistry(server.getPort());
					RemoteMethods stub = (RemoteMethods) registry.lookup("ServerHandler");

					boolean response = stub.appendEntries(term, 1, 0, 0, null, 0);

					queue.add(response);
				}				

			} catch (RemoteException | NotBoundException e) {
				System.out.println("Connection to server failed, retrying...");
			}
		}
	}
}
