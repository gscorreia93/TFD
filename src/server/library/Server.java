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

	public Server (String address, int port, int serverID,
			BlockingQueue<Request> requestQueue, Queue<Response> responseQueue, Queue<Response> voteQueue) {

		this.address = address;
		this.port = port;
		this.state = ServerState.FOLLOWER;
		this.serverID = serverID;
		this.requestQueue = requestQueue;
		this.responseQueue = responseQueue;
		this.voteQueue = voteQueue;
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

	public int getServerID() {
		return serverID;
	}

	public BlockingQueue<Request> getRequestQueue() {
		return requestQueue;
	}

	public Queue<Response> getResponseQueue() {
		return responseQueue;
	}

	public Queue<Response> getVoteQueue() {
		return voteQueue;
	}

	public void setState(ServerState newState){
if (!this.state.equals(newState)) System.out.println(port + "  New State = " + newState);
		this.state = newState;
	}
}
