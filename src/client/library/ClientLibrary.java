package client.library;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;

import server.library.Entry;
import server.library.RemoteMethods;
import server.library.Request;
import server.library.Response;
import server.library.Server;

public class ClientLibrary {

	private RemoteMethods stub;

	public boolean connectToServer(String clientID){

		String[] serverData=null;
		String serverAddress="";
		int port=0;
		BufferedReader br=null;
		try {
			br = new BufferedReader(new FileReader("src/server/library/servers.txt"));
			serverData = br.readLine().split(":");
			serverAddress = serverData[0];
			port = Integer.parseInt(serverData[1]);
			br.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			Registry registry = LocateRegistry.getRegistry(serverAddress,port);
			stub = (RemoteMethods) registry.lookup("ServerHandler");
			System.out.println("I'm "+clientID+" --- Connected to "+serverAddress+"["+port+"]");
			return true;
		} catch (RemoteException e) {
			System.err.println("Failed to connect to "+serverAddress+"["+port+"]");
		} catch (NotBoundException e) {
			e.printStackTrace();
		}

		return false;
	}


	public String request(String clientID, String command) {

		String message=null;

		Entry[] entries = new Entry[]{new Entry(clientID,UUID.randomUUID().toString(),command)};

		Response response;
		try {
			response = stub.appendEntries(-1, 0, 0, 0, entries, 0);
			message = "success: " + response.isSuccessOrVoteGranted();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(message);

		return message;
	}
}
