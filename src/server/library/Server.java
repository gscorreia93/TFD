package server.library;

enum State {
	LEADER,
	FOLLOWER,
	CANDIDATE
}

public class Server {

	private int port;
	private String address;
	private State state;
	
	public Server (String address, int port){
		this.address = address;
		this.port = port;
		this.state = State.FOLLOWER;
	}

	public int getPort() {
		return port;
	}
	
	public State getState(){
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
	
	public void setState(State newState){
		this.state = newState;
		System.out.println(port+"  New State = "+newState);
	}
	
}
