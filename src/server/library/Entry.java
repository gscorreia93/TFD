package server.library;

import java.io.Serializable;

public class Entry implements Serializable {

	private static final long serialVersionUID = 1L;

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
