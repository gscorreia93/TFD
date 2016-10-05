package client.library;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import server.library.RemoteMethods;

public class ClientLibrary {

	public String request(String clientID, int port, String command) {
		
		String message=null;
		
		try {
			Registry registry = LocateRegistry.getRegistry(port);
			RemoteMethods stub = (RemoteMethods) registry.lookup("ServerHandler");

			//String response = stub.executeCommand(clientID, command);
			//message = "response: " + response;

			System.out.println(message);

		} catch (Exception e) {
			System.out.println("Client exception: " + e.toString());
			e.printStackTrace();
		}
		return message;
	}
}
