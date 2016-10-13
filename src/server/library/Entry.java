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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clientID == null) ? 0 : clientID.hashCode());
		result = prime * result + ((entry == null) ? 0 : entry.hashCode());
		result = prime * result + ((requestID == null) ? 0 : requestID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entry other = (Entry) obj;
		if (clientID == null) {
			if (other.clientID != null)
				return false;
		} else if (!clientID.equals(other.clientID))
			return false;
		if (entry == null) {
			if (other.entry != null)
				return false;
		} else if (!entry.equals(other.entry))
			return false;
		if (requestID == null) {
			if (other.requestID != null)
				return false;
		} else if (!requestID.equals(other.requestID))
			return false;
		return true;
	}
}
