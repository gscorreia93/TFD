package server.library;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public class Server {

	private int port;
	private String address;
	private ServerState state;
	private int serverID;
	private BlockingQueue<Request> requestQueue;
	private Queue<Object> responseQueue;
	
	public Server (String address, int port, int serverID, BlockingQueue<Request> requestQueue, Queue<Object> responseQueue){
		this.address = address;
		this.port = port;
		this.state = ServerState.FOLLOWER;
		this.serverID = serverID;
		this.requestQueue = requestQueue;
		this.responseQueue = responseQueue;
	}

	public int getPort() {
		return port;
	}
	
	public ServerState getState(){
		return state;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public int getServerID(){
		
		return serverID;
	}
	
	public BlockingQueue<Request> getRequestQueue() {
		return requestQueue;
	}

	public Queue<Object> getResponseQueue() {
		return responseQueue;
	}

	public void setState(ServerState newState){
		this.state = newState;
		System.out.println(port+"  New State = "+newState);
	}
	
}
