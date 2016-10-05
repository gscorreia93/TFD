package client;

import java.util.UUID;
import client.library.*;

public class Client {

	public static void main(String[] args) {
		
		final String clientID = UUID.randomUUID().toString();
		
		ClientLibrary c = new ClientLibrary();
		
		c.request(clientID, 8081, "Run");
	}
}
