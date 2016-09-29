package client.library;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import server.library.rm.RemoteMethods;

public class Client {

	public String request(String clientID, int port, String command) {
		String result;
		try {
			Registry registry = LocateRegistry.getRegistry(port);
			RemoteMethods stub = (RemoteMethods) registry.lookup("ServerHandler");

			String response = stub.executeCommand(clientID, command);
			result = "response: " + response;

			System.out.println(result);

		} catch (Exception e) {
			result = "Client exception: " + e.toString();
			e.printStackTrace();
		}
		return result;
	}
}
