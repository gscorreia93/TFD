package common;

import java.util.ArrayList;
import java.util.List;

import server.library.util.Server;

public enum RAFTServers {
	INSTANCE;

	private Server currentServer;
	private List<Server> servers;

	private RAFTServers() {
		servers = new ArrayList<>();

		loadServers();
	}

	private void loadServers() {
		Server server = new Server();
		server.setServerID(1);
		server.setPort(8082);
		server.setAddress("localhost");
		servers.add(server);

		server = new Server();
		server.setServerID(2);
		server.setPort(8083);
		server.setAddress("localhost");
		servers.add(server);

		server = new Server();
		server.setServerID(3);
		server.setPort(8084);
		server.setAddress("localhost");
		servers.add(server);

		System.out.println("Servers loaded");
	}

	public List<Server> getServers() {
		return servers;
	}

	public Server getCurrentServer() {
		return currentServer;
	}
	public void setCurrentServer(Server currentServer) {
		this.currentServer = currentServer;
	}
	
}
