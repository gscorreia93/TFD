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

	public static final int COMMIT_LOG = -2;
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

			while (true) {
				if (server.getState() == ServerState.LEADER || server.getState() == ServerState.CANDIDATE) {

					for (Server sv : servers) {
						ServerThread thread = new ServerThread(sv);
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
			try {
				Registry registry = LocateRegistry.createRegistry(server.getPort());
				registry.bind("ServerHandler", this);

				System.out.println(server.getAddress() + "[" + server.getPort() + "] started!");
				return server;

			} catch (RemoteException e) {
				System.err.println(server.getPort() + " already bounded, trying another port.");
			} catch (AlreadyBoundException e) {
				System.err.println(server.getPort() + " already bounded, trying another port.");
			}
		}
		throw new ServerNotFoundException();
	}

	public synchronized Response requestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws RemoteException {
		
		if (term > eh.getTerm() && !eh.hasVoted()) {
			if (server.getState() != ServerState.FOLLOWER) {
				server.setState(ServerState.FOLLOWER);
			}

			eh.setTerm(term);
			eh.resetTimer();
			eh.resetState();
			eh.setVoted(true);

			return new Response(eh.getTerm(), true);

		} else if(server.getState() == ServerState.CANDIDATE && (candidateID == server.getServerID())) {
			eh.setVoted(true);

			return new Response(eh.getTerm(), true);
		}
		return new Response(eh.getTerm(), false);
	}

	public Response appendEntries(int term, int leaderId, int prevLogIndex, int prevLogTerm, Entry[] entries, int leaderCommit) throws RemoteException {
		Response response = null;

		if (term == CLIENT_REQUEST) { // If it is a Client Request
			response = new LogReplication(server, raftServers.getServers()).leaderReplication(entries, eh.getTerm());

		// Commits a log in all servers
		} else if (term == COMMIT_LOG) {

			response = new LogReplication(server, raftServers.getServers()).commitLog(leaderCommit, eh.getTerm());


		// When a follower receives a request to appendEntries
		} else if (entries != null && server.getState() != ServerState.LEADER) {

			response = new LogReplication(server, raftServers.getServers()).followerReplication(term,
					leaderId, prevLogIndex, prevLogTerm, entries, leaderCommit, eh.getTerm());


		// When a heartbeat is received
		} else if (entries == null && server.getState() != ServerState.LEADER) {

			if (server.getState() != ServerState.FOLLOWER){
				eh.setServerState(ServerState.FOLLOWER);

				for (int i = 0; i < threadPool.length; i++){
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

			eh.resetTimer();
			eh.resetState();
		}

		return response;
	}

	/**
	 * Keeps a thread running for a server
	 */
	private class ServerThread extends Thread {

		private BlockingQueue<Request> requestQueue;
		private Queue<Response> responseQueue;
		private Queue<Response> voteQueue;
		private Server server;

		public ServerThread(Server server) {
			this.requestQueue = server.getRequestQueue();
			this.responseQueue = server.getResponseQueue();
			this.voteQueue = server.getVoteQueue();
			this.server = server;
		}

		@Override
		public void run() {
			Registry registry;
			RemoteMethods stub;

			while (true) {
				try { // Tries to connect to a server
					registry = LocateRegistry.getRegistry(server.getPort());
					stub = (RemoteMethods) registry.lookup("ServerHandler");
					break;
				} catch (Exception e) { }
			}

			while (true) { // Keeps checking the requestQueue to see if there are more requests
				try {
					Request rq = requestQueue.take();

					boolean sent = false;

					while (!sent) {
						if (rq.getClass() == RequestVoteRequest.class) {

							RequestVoteRequest typedRequest = (RequestVoteRequest) rq;

							try {
								Response response = stub.requestVote(typedRequest.getTerm(), typedRequest.getServerId(), typedRequest.getLastLogIndex(), typedRequest.getLastLogTerm());
																
								synchronized (response) {
									voteQueue.add(response);
									sent = true;
								}

							} catch (RemoteException e) {
								System.err.println("Connection failed, retrying...");
							}


						} else if (rq.getClass() == AppendEntriesRequest.class) {

							AppendEntriesRequest typedRequest = (AppendEntriesRequest) rq;

							try {
								Response response = stub.appendEntries(typedRequest.getTerm(), typedRequest.getServerId(),
										typedRequest.getLastLogIndex(), typedRequest.getLastLogTerm(), typedRequest.getEntries(), typedRequest.getLeaderCommit());

								if (response != null) {
									// To replicate a log
									synchronized (response) {
										responseQueue.add(response);
										sent = true;
									}

								} else { // heartbeat
									sent = true;
								}

							} catch (RemoteException e) { }
						}
					}

				} catch (InterruptedException e) {
					e.printStackTrace();

				} // eof try
			} // eof while (true)

		} // eof run
	} // eof class ServerThread

}
