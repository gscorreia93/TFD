package client;

import java.util.Scanner;
import java.util.UUID;

import client.library.*;

public class Client {

	public static void main(String[] args) {

		final String clientID = UUID.randomUUID().toString().substring(0, 7);

		ClientLibrary c = new ClientLibrary();

		if (c.connectToServer(clientID)){
			String entry = null;
			Scanner s = new Scanner(System.in);

			while (true) { 
				System.out.println("Insert command ('q' to exit): ");
				entry = s.nextLine();

				if (entry.equalsIgnoreCase("q") || entry==null)
					break;

				c.request(clientID, entry);
			}

			System.out.println(clientID+" terminated");

			s.close();
		}
	}
}
