package server.library;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import server.library.log.LogEntry;
import server.library.log.LogHandler;

public class ServerHandler extends UnicastRemoteObject implements RemoteMethods {

	private static final long serialVersionUID = 1L;

	public static final int COMMIT_LOG = -2;
	private final int CLIENT_REQUEST = -1;

	private RAFTServers raftServers;

	private Server server;
	private ElectionHandler eh;
	private ServerThreadPool serverThreadPool;
	private Queue<Entry> entryQueue;
	private LogHandler logHandler;

	private int leaderID;

	public ServerHandler() throws RemoteException {
		super();

		raftServers = new RAFTServers();
		entryQueue = new LinkedList<Entry>();
	}

	public void openConnection() {
		server = startServer(raftServers.getServers());

		if (server != null) {

			List<Server> servers = raftServers.getServers();
			serverThreadPool = new ServerThreadPool(servers.size());

			serverThreadPool.startThreads(servers);

			eh = new ElectionHandler(server, raftServers, serverThreadPool, entryQueue);
			eh.startElectionHandler();

			// Starts the log handler
			logHandler = new LogHandler("LOG_" + server.getAddress() + "_" + server.getPort());

		} else {
			System.err.println("Incorrect server configuration, server not starting");
			System.exit(-1);
		}
	}

	private Server startServer(List<Server> servers) {
		if (servers == null || servers.isEmpty()) {
			System.err.println("There are no more available servers to connect");
			return null;
		}

		for (Server server : servers) {
			try {
				if (server.getAddress().equals(InetAddress.getLocalHost().getHostAddress()) || server.getAddress().equals("localhost") || server.getAddress().equals("127.0.0.1")){
					Registry registry = LocateRegistry.createRegistry(server.getPort());
					registry.bind("ServerHandler", this);
					System.out.println(server.getAddress()+":"+server.getPort()+" started!");
					return server;
				}
			} catch (RemoteException e) {
				System.err.println(server.getPort() + " already bounded, trying another port.");
			} catch (AlreadyBoundException e) {
				System.err.println(server.getPort() + " already bounded, trying another port.");
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public synchronized Response requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws RemoteException {

		Response response = new Response(eh.getTerm(), false);

		if (term > eh.getTerm()) {
			eh.setTerm(term);
			eh.resetState();
			eh.setVoted(true);

			serverThreadPool.interruptThreads();
			serverThreadPool.purgeQueues();

			if (!eh.isFollower() && candidateID != server.getServerID()) {

				eh.setServerState(ServerState.FOLLOWER);
			}
			response = new Response(eh.getTerm(), true);

		} else if (server.getState() == ServerState.CANDIDATE && (candidateID == server.getServerID())) {
			eh.setVoted(true);
			response = new Response(eh.getTerm(), true);
		}

		// A server can vote only once for a term
		if (eh.hasVotedForTerm(term)) {
			response = new Response(eh.getTerm(), false);
		}
		return response;
	}

	public Response appendEntries(int term, int leaderId, int prevLogIndex, int prevLogTerm, Entry[] entries, int leaderCommit) throws RemoteException {
		Response response = null;

		if (term == CLIENT_REQUEST) { // FROM CLIENT

			if(server.getState() != ServerState.LEADER) {  //NOT LEADER
				System.err.println("Redirecting to Leader (" + leaderID + ")" );
				response = new Response(-1, false);
				response.setLeaderID(leaderID);

			} else {  // LEADER
				if(entries != null && entries.length != 0){
					logHandler.writeLogEntries(entries,eh.getTerm());

					//adiciona as entries ah queue para serem despachadas quando tiverem tempo de processamento
					for (int i = 0; i < entries.length; i++) {
						entryQueue.add(entries[i]);
					}

					List<Server> servers = raftServers.getServers();
					int quorum = (servers.size() / 2) + 1;

					Queue<Response> responseQueue = server.getResponseQueue();

					int voteSuccess = 0;
					int totalVoteCount = 0;

					System.out.println(entries[0].getClientID() + " says '" + entries[0].getEntry() + "'");
					if (!responseQueue.isEmpty()) {
						//por cada entry vai percorrer a queue ah procura das respostas dessa entry
						while (totalVoteCount < quorum && (voteSuccess < quorum)) {  
							for (Response element : responseQueue) {
								if (element.getRequestID().equals(entries[0].getRequestID())){
									if (element.isSuccessOrVoteGranted()){
										response = element;
										voteSuccess++;
									}
									totalVoteCount++;
								}
							}
						}
						responseQueue.remove(response);

						if(voteSuccess >= quorum){
							response = new Response(eh.getTerm(), true);
						} else {	
							response = new Response(eh.getTerm(), false);
						}

						logHandler.getLastLogEntry().setCommited(true);

						voteSuccess = 0;
						totalVoteCount = 0;
					}
				}
				else{
					response = new Response(eh.getTerm(), true);
				}

			}

			// Commits a log in all servers
		} else if (term == COMMIT_LOG) { 
			//response = new LogReplication(server, raftServers.getServers(), lh).commitLog(leaderCommit, eh.getTerm());


			// When a follower receives a request to appendEntries
		} else {  

			leaderID = leaderId;

			if(entries != null && entries.length != 0){
				System.out.println("term: "+ term+ "leaderID: "+leaderId+ "prevLogIndex: "+prevLogIndex+ "prevLogTerm: "+prevLogTerm+ "leaderCommit: "+ leaderCommit);
				
							
				logHandler.writeLogEntries(entries, term);
				
				System.out.println("Replication");	
				//	response = logHandler.followerReplication(prevLogTerm, leaderId, prevLogIndex, prevLogTerm, entries, leaderCommit, term);
				
				
				
				return new Response(eh.getTerm(), true);
			}

			if (server.getState() != ServerState.FOLLOWER){
				eh.setServerState(ServerState.FOLLOWER);
				serverThreadPool.interruptThreads();
				serverThreadPool.purgeQueues();
			}

			eh.resetState();
		}

		if(response != null && response.getLeaderID() == 0){
			response.setLeaderID(server.getServerID());
		}

		return response;
	}
}
