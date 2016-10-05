package server.library;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteMethods extends Remote {
	
	Response requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws RemoteException;
	
	public Response appendEntries(int term, int candidateID, int lastLogIndex, int lastLogTerm, Entry[] entries, int leaderCommit) throws RemoteException;	
}
