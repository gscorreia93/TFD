package server.library;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Class to store the servers after loaded.
 * Should load the servers only at startup and then keep them in memory.
 * 
 * TODO Load servers from file
 */
public class RAFTServers {

	private List<Server> servers;

	public RAFTServers() {
		servers = new ArrayList<Server>();
		loadServers();
	}

	private void loadServers() {
		Queue<Response> voteQueue =  new ArrayBlockingQueue<Response>(20);

		servers.add(new Server("localhost", 8081, 1, new ArrayBlockingQueue<Request>(20), new ArrayBlockingQueue<Response>(20), voteQueue));
		servers.add(new Server("localhost", 8082, 2, new ArrayBlockingQueue<Request>(20), new ArrayBlockingQueue<Response>(20), voteQueue));
		servers.add(new Server("localhost", 8083, 3, new ArrayBlockingQueue<Request>(20), new ArrayBlockingQueue<Response>(20), voteQueue));
	}

	public List<Server> getServers() {
		return servers;
	}
}
