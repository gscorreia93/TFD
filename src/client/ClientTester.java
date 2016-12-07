package client;

import java.util.Random;

import client.library.ClientLibrary;

public class ClientTester extends Thread{
	
	private String clientID;
	private ClientLibrary cl;
	private int messageNumber;
	private int timeBetweenRequests;
	
	public ClientTester(String clientID, ClientLibrary cl, int messageNumber, int timeBetweenRequests){
		
		this.clientID = clientID;
		this.cl = cl;
		this.messageNumber = messageNumber;
		this.timeBetweenRequests = timeBetweenRequests;
	}
	
	protected void connectToServer(){
		
		cl.connectToServer(clientID);
	}
	
	protected boolean request(String entry){
		
		return cl.request(clientID, entry);
	}
	
	@Override
	public void run() {
		
		if(cl.connectToServer(clientID)){		
			String entry = null;

			while (messageNumber > 0) {
				
				entry = randomCommandGenerator();
				System.out.println("Command: " + entry);

				try {
				    Thread.sleep(timeBetweenRequests);
				} catch(InterruptedException ex) {
				    Thread.currentThread().interrupt();
				}
				
				
				cl.request(clientID, entry);
				messageNumber--;
				
				System.out.println(messageNumber);
			}
			
			System.out.println("ENDED " + this.getName());
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
