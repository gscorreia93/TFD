package client.library;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.UUID;
  
import server.library.Entry;
import server.library.RemoteMethods;
import server.library.Response;

public class ClientLibrary {

	private RemoteMethods stub;

	private ArrayList<String> servers = null;

	/*
	 * Connecta-se ao primeiro servidor da lista que tiver online
	 */
	public boolean connectToServer(String clientID){

		if(servers==null){
			readServersFile();
		}

		String[] serverData= null;
		String serverAddress="";
		int port=0;

		for (String server : servers) {
			serverData = server.split(":");
			serverAddress = serverData[0];
			port = Integer.parseInt(serverData[1]);
			if (registryToServer(clientID,serverAddress, port))
				return true;
		}
	
		return false;
	}

	public String request(String clientID, String command) {

		String message=null;

		Entry[] entries = new Entry[]{new Entry(clientID,UUID.randomUUID().toString(),command)};

		Response response=null;
		
		String[] serverData= null;
	 
		boolean sendedToLeader = false;
		
		while(!sendedToLeader){
			try {
				response = stub.appendEntries(-1, 0, 0, 0, entries, 0);
				//fez o pedido a um follower
				if (response.getTerm() == -1 && response.isSuccessOrVoteGranted() == false){
					System.err.println("Follower :(    Trying Leader...");
					serverData = servers.get(response.getLeaderID()-1).split(":");
					registryToServer(clientID,serverData[0], Integer.parseInt(serverData[1]));
					response = stub.appendEntries(-1, 0, 0, 0, entries, 0);
				}
				sendedToLeader = true;
			} catch (RemoteException e) {
				//o servidor da ligação crashou
				System.err.println("Failed to receive response from server\nTrying another server...");
				connectToServer(clientID);
			}
		}
		
		message = "success: " + response.isSuccessOrVoteGranted();
		System.out.println(message);
	

		return message;
	}

	private void readServersFile(){
		String line="";

		servers = new ArrayList<String>();
		BufferedReader br=null;
		try {
			br = new BufferedReader(new FileReader("src/server/library/servers.txt"));
			while((line = br.readLine())!=null){
				servers.add(line);
			}
			br.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private boolean registryToServer(String clientID, String address,int port){
		try {
			Registry registry = LocateRegistry.getRegistry(address,port);
			stub = (RemoteMethods) registry.lookup("ServerHandler");
			System.out.println("I'm "+clientID+" --- Connected to "+address+":"+port);
			return true;
		} catch (RemoteException e) {
			System.err.println("Failed to connect to "+address+":"+port);
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		return false;
	}
}