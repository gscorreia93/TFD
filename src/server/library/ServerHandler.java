package server.library;

import java.net.ConnectException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import exceptions.ServerNotFoundException;

public class ServerHandler extends UnicastRemoteObject implements RemoteMethods {

	private static final long serialVersionUID = 1L;
	private ElectionHandler eh;
	private Server server;
	private Thread[] threadPool;

	public ServerHandler() throws RemoteException {
		super();
	}

	public void openConnection() {
		server = startServer(RAFTServers.INSTANCE.getServers());
		
		if (server != null) {
			
			List<Server> servers = RAFTServers.INSTANCE.getServers();
			threadPool = new Thread[servers.size()];
			
			eh = new ElectionHandler(server);
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

	public boolean requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws RemoteException {
		
		if (eh.hasVoted() || term < eh.getTerm() || eh.getState() == ServerState.LEADER
				|| (eh.getState() == ServerState.CANDIDATE && server.getServerID() != candidateID)) {
			return false;
		}

		System.out.println("EU VOTEI: " + candidateID);

		eh.setVoted();
		return true;
	}

	public boolean appendEntries(int term, int candidateID, int lastLogIndex, int lastLogTerm, Entry[] entries,
			int leaderCommit) throws RemoteException {
		
		
		if (entries == null && server.getState() != ServerState.LEADER) {

			System.out.println("YES MASTER");
			
			if(server.getState() != ServerState.FOLLOWER){
				eh.setServerState(ServerState.FOLLOWER);
				
				for(int i = 0; i < threadPool.length; i++){
					System.out.println("stopping thread");
					if(threadPool[i] != null){
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
			
			return true;
		}

		return false;
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
		private Queue<Object> responseQueue;
		private Server server;

		public ServerThread(BlockingQueue<Request> requestQueue, Queue<Object> responseQueue, Server server) {

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
					System.out.println("Connection failed, retrying...");
				}
			}
			
			while (true) {
				try {
											
					Request rq = requestQueue.take();
					boolean sent = false;
																	
					if (rq.getClass() == RequestVoteRequest.class) {
						
						RequestVoteRequest typedRequest = (RequestVoteRequest) rq;

						
						while(!sent){
							try {
								boolean response = stub.requestVote(typedRequest.getTerm(), typedRequest.getServerId(), typedRequest.getLastLogIndex(), typedRequest.getLastLogTerm());
							
								responseQueue.add(response);
								sent = true;
							
							} catch (RemoteException e) {
								System.out.println("Connection failed, retrying...");
							}
						}
						
					} else if (rq.getClass() == AppendEntriesRequest.class) {
						
						AppendEntriesRequest typedRequest = (AppendEntriesRequest) rq;
						
						try {
							boolean response = stub.appendEntries(typedRequest.getTerm(), typedRequest.getServerId(), typedRequest.getLastLogIndex(), typedRequest.getLastLogTerm(), typedRequest.getEntries(), typedRequest.getLeaderCommit());
							sent = true;
							
						} catch (RemoteException e) {
							System.out.println("Connection failed, retrying...");
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
