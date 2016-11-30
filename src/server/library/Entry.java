package server.library;

import java.io.Serializable;

public class Entry implements Serializable {

	private static final long serialVersionUID = 1L;

	private String entry;
	private String clientID;
	private String requestID;
	private int term;
	private boolean commited;

	public Entry(String clientID, String requestID, String entry) {
		this.entry = entry;
		this.clientID = clientID;
		this.requestID = requestID;

		this.term = 0;
		this.commited = false;
	}

	public Entry(String clientID, String requestID, String entry, int term, boolean commited) {
		this.entry = entry;
		this.clientID = clientID;
		this.requestID = requestID;
		this.term = term;
		this.commited = commited;
	}

	public boolean isCommited() {
		return commited;
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

	public int getTerm() {
		return term;
	}

	public void setCommited(boolean commited) {
		this.commited = commited;
	}

	public void setTerm(int term) {
		this.term = term;
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
