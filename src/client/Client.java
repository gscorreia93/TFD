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

			help();

			while (true) { 
				System.out.print("Command: ");
				entry = s.nextLine();

				if (entry == null || entry.equalsIgnoreCase("q"))
					break;

				if (entry.equalsIgnoreCase("h")) {
					help();
					continue;
				}

				// To avoid validations on quick tests
				c.request(clientID, entry);
				
				// Uncomment for the delivery
				// if (entryIsValid(entry)) {
					// c.request(clientID, entry);
				// }
			}

			System.out.println(clientID + " terminated");

			s.close();
		}
	}

	private static boolean entryIsValid(String entry) {
		entry = entry.trim().replaceAll(" +", " ");

		if (entry.equalsIgnoreCase("list")) {
			return true;
		}

		String parts[] = entry.split(" ");
		String command = parts[0].toLowerCase();
		
		if ((command.equals("get") || command.equals("put")
				|| command.equals("del")) && parts.length == 2) {
			return true;
		}
		
		if (command.equals("cas")) {
			System.err.println("not implemented");
			return false;
		}
		
		System.err.println("Invalid command");
		return false;
	}

	private static void help() {
		System.out.println("");
		System.out.println("Available commands (q to quit, h to help):");
		System.out.println("list \t Lists the available contents");
		System.out.println("get i \t Gets an entry at index i");
		System.out.println("del i \t Deletes an entry at index i");
		System.out.println("put e \t Adds an entry e");
		System.out.println("cas \t ?");
		System.out.println("-------------------------------------");
	}
}
