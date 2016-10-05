package server.library;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import server.library.log.LogReplication;
import exceptions.ServerNotFoundException;

public class ServerHandler extends UnicastRemoteObject implements RemoteMethods {

	private static final long serialVersionUID = 1L;
	
	private final int CLIENT_REQUEST = -1;
	
	private RAFTServers raftServers;
	private ElectionHandler eh;
	private Server server;
	private Thread[] threadPool;

	public ServerHandler() throws RemoteException {
		super();
		raftServers = new RAFTServers();
	}

	public void openConnection() {
		server = startServer(raftServers.getServers());

		if (server != null) {

			List<Server> servers = raftServers.getServers();
			threadPool = new Thread[servers.size()];

			eh = new ElectionHandler(server, raftServers);
			eh.startElectionHandler();

			while(true){
				if(server.getState() == ServerState.LEADER || server.getState() == ServerState.CANDIDATE){

					for(Server sv : servers){
						ServerThread thread = new ServerThread(sv.getRequestQueue(), sv.getResponseQueue(), sv);
						thread.start();
					}

					break;
				}

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
			}
		} else {
			System.out.println("Incorrect server configuration, server not starting");
			System.exit(-1);
		}
	}

	private Server startServer(List<Server> servers) {
		if (servers == null || servers.isEmpty()) {
			throw new ServerNotFoundException();
		}

		for (Server server : servers) {
			System.out.println(server.getPort());
			try {
				// RemoteMethods stub = (RemoteMethods)
				// UnicastRemoteObject.exportObject(this,server.getPort());
				Registry registry = LocateRegistry.createRegistry(server.getPort());
				registry.bind("ServerHandler", this);

				System.out.println(server.getAddress() + "[" + server.getPort() + "] started!");

				return server;

			} catch (RemoteException e) {
				System.out.println("Port already bounded, trying another port.");
			} catch (AlreadyBoundException e) {
				System.out.println("Port already bounded, trying another port.");
			}
		}

		return null;
	}

	public synchronized Response requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws RemoteException {
		
		if(term > eh.getTerm() && !eh.hasVoted()){
			if(server.getState() != ServerState.FOLLOWER){
				server.setState(ServerState.FOLLOWER);
			}
			eh.setTerm(term);
			eh.resetTimer();
			eh.resetState();
			eh.setVoted(true);
			
//			System.out.println("EU VOTEI1: " + candidateID + " NO TERM: " + eh.getTerm() + " vindo do term: " + term);
//			System.out.flush();
						
			return new Response(eh.getTerm(), true);
			
		}else if(server.getState() == ServerState.CANDIDATE && (candidateID == server.getServerID())){
			eh.setVoted(true);
			
//			System.out.println("EU VOTEI2: " + candidateID + " NO TERM: " + eh.getTerm() + " vindo do term: " + term);
//			System.out.flush();
			
			return new Response(eh.getTerm(), true);
		}
		
			return new Response(eh.getTerm(), false);
	}

	public Response appendEntries(int term, int leaderId, int prevLogIndex, int prevLogTerm, Entry[] entries, int leaderCommit) throws RemoteException {
		
		Response response = null;
		
		if (term == CLIENT_REQUEST) { // If it is a Client Request
		
			response = new LogReplication(server).leaderReplication(entries, eh.getTerm());
		
			// When a heartbeat is received
		} else if (entries == null && server.getState() != ServerState.LEADER) {

			 //System.out.println("YES MASTER");
			
			if(server.getState() != ServerState.FOLLOWER){
				eh.setServerState(ServerState.FOLLOWER);
				
				for(int i = 0; i < threadPool.length; i++){
					//System.out.println("stopping thread");
					
					if (threadPool[i] != null){
						threadPool[i].interrupt();
						try {
							threadPool[i].join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
			
			eh.setHasLeader();
			eh.resetTimer();
			eh.resetState();
			
			// When a follower receives a request to appendEntries
		} else if (server.getState() != ServerState.LEADER) {
			
			response = new LogReplication(server).followerReplication(term,
					leaderId, prevLogIndex, prevLogTerm, entries, leaderCommit, eh.getTerm());
		}

		return response;
	}

	public String connect2Server() throws RemoteException {
		return null;
	}

	public String executeCommand(String clientID, String command) throws RemoteException {

		System.out.println("CLIENT RPC RECIEVED");
		return clientID + ": " + command;
	}

	private class ServerThread extends Thread {

		private BlockingQueue<Request> requestQueue;
		private Queue<Response> responseQueue;
		private Server server;

		public ServerThread(BlockingQueue<Request> requestQueue, Queue<Response> responseQueue, Server server) {

			this.requestQueue = requestQueue;
			this.responseQueue = responseQueue;
			this.server = server;
		}

		@Override
		public void run() {

			Registry registry;
			RemoteMethods stub;

			while(true){
				try {
					registry = LocateRegistry.getRegistry(server.getPort());
					stub = (RemoteMethods) registry.lookup("ServerHandler");
					break;
				} catch (Exception e) {
					//System.out.println("Connection failed, retrying...");
				}
			}

			while (true) {
				try {
					Request rq = requestQueue.take();
					boolean sent = false;

					while(!sent){
						if (rq.getClass() == RequestVoteRequest.class) {

							RequestVoteRequest typedRequest = (RequestVoteRequest) rq;

							try {
								Response response = stub.requestVote(typedRequest.getTerm(), typedRequest.getServerId(), typedRequest.getLastLogIndex(), typedRequest.getLastLogTerm());
																
								synchronized (response) {
									responseQueue.add(response);
									sent = true;
								}
								

							} catch (RemoteException e) {
								e.printStackTrace();
								System.out.println("Connection failed, retrying...");
							}


						} else if (rq.getClass() == AppendEntriesRequest.class) {

							AppendEntriesRequest typedRequest = (AppendEntriesRequest) rq;

							try {
								Response response = stub.appendEntries(typedRequest.getTerm(), typedRequest.getServerId(), typedRequest.getLastLogIndex(), typedRequest.getLastLogTerm(), typedRequest.getEntries(), typedRequest.getLeaderCommit());
								sent = true;

							} catch (RemoteException e) {
								//System.out.println("Connection failed, retrying...");
							}
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
