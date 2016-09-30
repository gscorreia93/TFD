package server.library;

import java.util.ArrayList;
import java.util.List;

public enum RAFTServers {
	INSTANCE;

	private List<Server> servers;

	private RAFTServers() {
		servers = new ArrayList<Server>();
		loadServers();
	}

	private void loadServers() {
		servers.add(new Server("localhost",8081));
		servers.add(new Server("localhost",8082));
		servers.add(new Server("localhost",8083));

		System.out.println("Servers loaded");
	}

	public List<Server> getServers() {
		return servers;
	}
	
}
