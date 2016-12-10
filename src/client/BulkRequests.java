package client;

import java.util.Random;
import java.util.UUID;

import client.library.ClientLibrary;

public class BulkRequests {

	public static void main(String[] args) {

		final String clientID = UUID.randomUUID().toString().substring(0, 7);

		ClientLibrary c = new ClientLibrary();

		if (c.connectToServer(clientID)){
			String entry = null;

			while (true) { 
				entry = randomCommandGenerator();
				System.out.println("Command: " + entry);

				try {
				    Thread.sleep(5000); // Each 5 seconds
				} catch(InterruptedException ex) {
				    Thread.currentThread().interrupt();
				}

				c.request(clientID, entry);
			}
		}
	}
	
	private static String randomCommandGenerator() {
		
		Random random = new Random();
		int option = random.nextInt(5) + 1;
		switch (option) {
			case 1: return "list";
			case 2: return "get " + random.nextInt(10);
			case 3: return "put " + random.nextInt(10);
			case 4: return "del " + random.nextInt(10);
			case 5: return "cas " + random.nextInt(10) + " " + random.nextInt(10) + " " + random.nextInt(10);
		}
		
		return "invalid";
	}
}
