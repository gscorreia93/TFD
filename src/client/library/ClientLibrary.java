package client.library;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.UUID;

import server.library.Entry;
import server.library.RemoteMethods;
import server.library.Response;

public class ClientLibrary {

	public String request(String clientID, int port, String command) {
		
		String message=null;
		
		try {
			Registry registry = LocateRegistry.getRegistry(port);
			RemoteMethods stub = (RemoteMethods) registry.lookup("ServerHandler");

			Entry[] entries = new Entry[]{new Entry(command, clientID, UUID.randomUUID().toString())};
			
			Response response = stub.appendEntries(-1, 0, 0, 0, entries, 0);
			message = "success: " + response.isSuccessOrVoteGranted();

			System.out.println(message);

		} catch (Exception e) {
			System.out.println("Client exception: " + e.toString());
			e.printStackTrace();
		}
		return message;
	}
}
