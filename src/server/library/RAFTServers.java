package server.library;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Class to store the servers after loaded.
 * Should load the servers only at startup and then keep them in memory.
 * 
 * TODO Load servers from file
 */
public class RAFTServers {

	private List<Server> servers;

	public RAFTServers() {
		servers = new ArrayList<Server>();
		loadServers();
	}

	private void loadServers() {
		Queue<Response> voteQueue =  new ArrayBlockingQueue<Response>(40);
		Queue<Response> responseQueue = new ArrayBlockingQueue<Response>(1000);

		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("src/server/library/servers.txt"));
					
			String serverAddress="";
			String [] serverData=null;
			int port=0;
			
			int numberOfServer=0;
			
			while((serverAddress = br.readLine())!=null){
				serverData = serverAddress.split(":");
				serverAddress = serverData[0];
				port = Integer.parseInt(serverData[1]);
				numberOfServer++;	
				servers.add(new Server(serverAddress, port, numberOfServer, new ArrayBlockingQueue<Request>(20), responseQueue, voteQueue));
			}
			
			br.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<Server> getServers() {
		return servers;
	}
}
