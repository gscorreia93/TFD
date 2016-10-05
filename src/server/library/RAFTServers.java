package server.library;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class RAFTServers {

	private List<Server> servers;

	public RAFTServers() {
		servers = new ArrayList<Server>();
		loadServers();
	}

	private void loadServers() {
		
		Queue<Response> responseQueue =  new ArrayBlockingQueue<Response>(20);
		
		servers.add(new Server("localhost",8081,1, new ArrayBlockingQueue<Request>(20), responseQueue));
		servers.add(new Server("localhost",8082,2, new ArrayBlockingQueue<Request>(20), responseQueue));
		servers.add(new Server("localhost",8083,3, new ArrayBlockingQueue<Request>(20), responseQueue));

		System.out.println("Servers loaded");
	}

	public List<Server> getServers() {
		return servers;
	}
	
}
