package server.library;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import common.RAFTServers;
import server.library.exception.ServerNotFoundException;
import server.library.rm.RemoteMethods;
import server.library.util.Server;

public class ServerHandler implements RemoteMethods {

	public void openConnection() {
		startServer(RAFTServers.INSTANCE.getServers());

		ElectionHandler.INSTANCE.startElectionHandler();
	}

	private void startServer(List<Server> servers) {
		if (servers == null || servers.isEmpty()) {
			throw new ServerNotFoundException();
		}

		int port = servers.get(0).getPort();

		try {
			RemoteMethods stub = (RemoteMethods) UnicastRemoteObject.exportObject(this, port);

			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.createRegistry(port);
			registry.bind("ServerHandler", stub);

			System.err.println("Server ready on port " + port);

			RAFTServers.INSTANCE.setCurrentServer(servers.get(0));

		} catch (Exception e) {
			System.err.println("Port " + port + " is busy");

			servers.remove(0); // Removes the busy port
			startServer(servers); // Tries again with the next port
		}
	}
	
	@Override
	public boolean requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws RemoteException {
		return candidateID == 1;
	}

	@Override
	public boolean appendEntries(int term, int candidateID, int lastLogIndex, int lastLogTerm, String[] entries, int leaderCommit) throws RemoteException {
		// First resets the state to follower
		ElectionHandler.INSTANCE.resetState();
		return false;
	}

	@Override
	public String connect2Server() throws RemoteException {
		return null;
	}

	@Override
	public String executeCommand(String clientID, String command) throws RemoteException {
		return clientID + ": " + command;
	}
}
