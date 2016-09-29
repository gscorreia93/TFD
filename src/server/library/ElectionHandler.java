package server.library;

import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import server.library.util.Server;
import utils.State;

import common.RAFTServers;

public enum ElectionHandler {
	INSTANCE;

	private Timer timer;

	private int term = 0;
	private State state = State.FOLLOWER;

	void startElectionHandler() {
		resetState();
	}

	boolean isLeader() {
		return state == State.LEADER;
	}

	void resetState() {
		timer = new Timer();

		// Time for election timeout
		long time2Wait = new Random().nextInt(2000) + 2000;

		timer.schedule(new TimerTask() { // On election timeout
			@Override
			public void run() {
				startElection(); // Starts a new election

				// Resets the state again
				resetState();
			}
		}, time2Wait);
	}
	
	void startElection() {
		// First increments the term
		term ++;
		// Transitions to candidate state
		state = State.CANDIDATE;
		
		// System.out.println("Current term " + term);
		List<Server> servers = RAFTServers.INSTANCE.getServers();

		
		for (Server server : servers ){
			new ServerConnection(server);
		}
	
	}
}
