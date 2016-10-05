package server.library;

public class Entry {

	private String entry;
	private String clientID;
	private String requestID;

	public Entry(String clientID, String requestID, String entry) {
		this.entry = entry;
		this.clientID = clientID;
		this.requestID = requestID;
	}

	public String getEntry() {
		return entry;
	}

	public String getClientID() {
		return clientID;
	}

	public String getRequestID() {
		return requestID;
	}
}
