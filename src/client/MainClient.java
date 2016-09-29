package client;

import java.util.UUID;

import client.library.Client;

public class MainClient {

	public static void main(String[] args) {
		final String clientID = UUID.randomUUID().toString();
		
		Client c = new Client();
		
		c.request(clientID, 8082, "Run");
	}
}
