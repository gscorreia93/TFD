package client;

import java.util.UUID;

import client.library.ClientLibrary;

public class BatchTester {

	public static void main(String[] args) {
		
		int numClients = 10, msgNum = 10, timeBetweenRequests = 0, checkpoint = 0;
		
		ClientTester[] ctArray = new ClientTester[numClients];
		
		for (int i = 0; i < ctArray.length; i++) {
			ClientTester ct = new ClientTester(UUID.randomUUID().toString().substring(0, 7), new ClientLibrary(), msgNum, timeBetweenRequests, checkpoint, numClients);
			ctArray[i] = ct;
			ctArray[i].setName("Client-Thread " + i);
		}
		
		for (int i = 0; i < ctArray.length; i++) {
			ctArray[i].start();
		}
	}
}
