package server.library;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public class Server {

	private int port;
	private String address;
	private ServerState state;
	private int serverID;
	private BlockingQueue<Request> requestQueue;
	private Queue<Response> responseQueue;
	private Queue<Response> voteQueue;

	protected Server (String address, int port, int serverID,
			BlockingQueue<Request> requestQueue, Queue<Response> responseQueue, Queue<Response> voteQueue) {

		this.address = address;
		this.port = port;
		this.state = ServerState.FOLLOWER;
		this.serverID = serverID;
		this.requestQueue = requestQueue;
		this.responseQueue = responseQueue;
		this.voteQueue = voteQueue;
	}

	protected int getPort() {
		
		return port;
	}

	protected ServerState getState(){
		
		return state;
	}

	protected String getAddress() {
		
		return address;
	}
	
	protected int getServerID() {
		
		return serverID;
	}

	protected BlockingQueue<Request> getRequestQueue() {
		
		return requestQueue;
	}

	protected Queue<Response> getResponseQueue() {
		
		return responseQueue;
	}

	protected Queue<Response> getVoteQueue() {
		
		return voteQueue;
	}

	protected void setState(ServerState newState){
		
		System.out.println(address + ":" + port + " is now " + newState);
		this.state = newState;
	}

	@Override
	public String toString() {
		
		return "serverID: " + serverID + ", " + address + ":" + port;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Server other = (Server) obj;
		if (serverID != other.serverID)
			return false;
		if (port != other.port)
			return false;
		if (!address.equals(address))
			return false;
		
		return true;
	}
}
