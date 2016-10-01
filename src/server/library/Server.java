package server.library;

public class Server {

	private int port;
	private String address;
	private ServerState state;
	
	public Server (String address, int port){
		this.address = address;
		this.port = port;
		this.state = ServerState.FOLLOWER;
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
	
	public void setState(ServerState newState){
		this.state = newState;
		System.out.println(port+"  New State = "+newState);
	}
	
}
