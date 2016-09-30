package server.library;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import exceptions.ServerNotFoundException;

public class ServerHandler extends  UnicastRemoteObject {

	protected ServerHandler() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	public void openConnection() {
		startServer(RAFTServers.INSTANCE.getServers());
		ElectionHandler.INSTANCE.startElectionHandler();
	}

	private void startServer(List<Server> servers) {
		if (servers == null || servers.isEmpty()) {
			throw new ServerNotFoundException();
		}

		for( Server server : servers){
			System.out.println(server.getPort());
			try {
				//RemoteMethods stub = (RemoteMethods) UnicastRemoteObject.exportObject(this,server.getPort());
				Registry registry = LocateRegistry.createRegistry(server.getPort());
				registry.bind("ServerHandler", this);

				System.out.println(server.getAddress()+"["+server.getPort()+"] started!");

			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (AlreadyBoundException e) {
				e.printStackTrace();
			}
		}
	}

//	@Override
//	public boolean requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws RemoteException {
//		return candidateID == 1;
//	}
//
//	@Override
//	public boolean appendEntries(int term, int candidateID, int lastLogIndex, int lastLogTerm, String[] entries, int leaderCommit) throws RemoteException {
//		// First resets the state to follower
//		ElectionHandler.INSTANCE.resetState();
//		return false;
//	}
//
//	@Override
//	public String connect2Server() throws RemoteException {
//		return null;
//	}
//
//	@Override
//	public String executeCommand(String clientID, String command) throws RemoteException {
//		return clientID + ": " + command;
//	}
}
