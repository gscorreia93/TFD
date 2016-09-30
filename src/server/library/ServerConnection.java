package server.library;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerConnection extends Thread {

	private final Server server;
	
	public ServerConnection(Server s) {
		this.server = s;
	}

	
	protected String sendRMI (String clientID, String message) throws RemoteException, NotBoundException{
		
		Registry registry = LocateRegistry.getRegistry(server.getPort());
		RemoteMethods stub = (RemoteMethods) registry.lookup("ServerHandler");
		
	    String result = stub.executeCommand(clientID, message);
		
		return result;
	}
	

	public Boolean requestVote(int term, int lastLogIndex, int lastLogTerm) {
		Boolean result = null;
		
		while (result == null){
			
			try {
				Registry registry = LocateRegistry.getRegistry(server.getPort());
				RemoteMethods stub = (RemoteMethods) registry.lookup("ServerHandler");

		//		result = stub.requestVote(term, server.getServerID(), lastLogIndex, lastLogTerm);

				System.out.println(result);

			} catch (Exception e) {
				// result = null
				e.printStackTrace();
			}
			
		}
		
		return result;
	}
}
