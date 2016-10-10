package client;

import java.util.Scanner;
import java.util.UUID;

import client.library.*;

public class Client {

	public static void main(String[] args) {

		final String clientID = UUID.randomUUID().toString();

		ClientLibrary c = new ClientLibrary();

		String entry = null;
		Scanner s = new Scanner(System.in);

		while (true) {
			System.out.println("Insert command :");
			entry = s.nextLine();

			if (entry != null && entry.equalsIgnoreCase("q"))
				break;

			c.request(clientID, 8081, entry);
		}

		System.out.println("Client terminated");

		s.close();
	}

}
