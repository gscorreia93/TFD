package server.library;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import exceptions.ServerNotFoundException;

public class ServerHandler extends UnicastRemoteObject implements RemoteMethods {

	private static final long serialVersionUID = 1L;
	private ElectionHandler eh;
	

	public ServerHandler() throws RemoteException {
		super();
		eh = new ElectionHandler();
	}

	public void openConnection() {
		startServer(RAFTServers.INSTANCE.getServers());
		eh.startElectionHandler();
	}

	private void startServer(List<Server> servers) {
		if (servers == null || servers.isEmpty()) {
			throw new ServerNotFoundException();
		}

		for(Server server : servers){
			System.out.println(server.getPort());
			try {
				//RemoteMethods stub = (RemoteMethods) UnicastRemoteObject.exportObject(this,server.getPort());
				Registry registry = LocateRegistry.createRegistry(server.getPort());
				registry.bind("ServerHandler", this);

				System.out.println(server.getAddress()+"["+server.getPort()+"] started!");
				
				break;

			} catch (RemoteException e) {
				System.out.println("Port already bounded, trying another port.");
			} catch (AlreadyBoundException e) {
				System.out.println("Port already bounded, trying another port.");
			}
		}
	}

	public boolean requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws RemoteException {

		if(term < eh.getTerm() || eh.getState() == ServerState.LEADER){
			return false;
		}
		
		return true;
	}

	public boolean appendEntries(int term, int candidateID, int lastLogIndex, int lastLogTerm, String[] entries, int leaderCommit) throws RemoteException {

		System.out.println("AppendEntries called");
		
		if(entries == null){

			return true;
		}
		
		return false;
	}

	public String connect2Server() throws RemoteException {
		return null;
	}

	public String executeCommand(String clientID, String command) throws RemoteException {
		return clientID + ": " + command;
	}
}
