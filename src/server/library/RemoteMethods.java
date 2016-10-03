package server.library;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteMethods extends Remote {

	String executeCommand(String clientID, String command) throws RemoteException;
	
	boolean requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws RemoteException;
	
	boolean appendEntries(int term, int candidateID, int lastLogIndex, int lastLogTerm, Entry[] entries, int leaderCommit) throws RemoteException;
	
	String connect2Server() throws RemoteException;
	
}
