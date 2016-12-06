package server.library;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import server.library.log.LogEntry;
import server.library.log.LogHandler;
import server.library.statemachine.IService;
import server.library.statemachine.StateMachine;

/**
 * Class that handles the requests from:
 * - clients to leader
 * - leader to followers
 */
public class ServerHandler extends UnicastRemoteObject implements RemoteMethods {

	private static final long serialVersionUID = 1L;

	public static final int COMMIT_LOG = -2;
	private final int CLIENT_REQUEST = -1;

	private RAFTServers raftServers;

	private Server server;
	private ElectionHandler eh;
	private LogHandler logHandler;

	private IService stateMachine;

	private Queue<Entry> entryQueue;
	private Queue<Entry> commitQueue;
	private ServerThreadPool serverThreadPool;

	private int leaderID;

	public ServerHandler() throws RemoteException {
		super();

		raftServers = new RAFTServers();
		entryQueue = new LinkedList<Entry>();
		commitQueue = new LinkedList<Entry>();
		stateMachine = new StateMachine();
	}

	/** 
	 * Starts the server and the threads to 
	 * communicate with other servers.
	 */
	public void openConnection() {
		server = startServer(raftServers.getServers());

		if (server != null) {

			List<Server> servers = raftServers.getServers();
			serverThreadPool = new ServerThreadPool(servers.size());

			serverThreadPool.startThreads(servers);

			// Starts the log handler
			logHandler = new LogHandler("LOG_" + server.getAddress() + "_" + server.getPort());

			eh = new ElectionHandler(server, raftServers, serverThreadPool, entryQueue, commitQueue, logHandler);
			eh.startElectionHandler();

		} else {
			System.err.println("Incorrect server configuration, server not starting");
			System.exit(-1);
		}
	}

	/**
	 * Starts this server to start awaiting for requests.
	 */
	private Server startServer(List<Server> servers) {
		if (servers == null || servers.isEmpty()) {
			System.err.println("There are no more available servers to connect");
			return null;
		}

		for (Server server : servers) {
			try {
				if (server.getAddress().equals(InetAddress.getLocalHost().getHostAddress())
						|| server.getAddress().equals("localhost") || server.getAddress().equals("127.0.0.1")) {

					Registry registry = LocateRegistry.createRegistry(server.getPort());
					registry.bind("ServerHandler", this);

					System.out.println(server.getAddress() + ":" + server.getPort() + " started!");
					return server;
				}
			} catch (RemoteException | AlreadyBoundException e) {
				System.err.println(server.getPort() + " already bounded, trying another port.");
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Handles the requestVote from other servers.
	 */
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

	/**
	 * Handles the requestVote from clientes and other servers.
	 */
	public Response appendEntries(int term, int leaderId, int prevLogIndex, int prevLogTerm, Entry[] entries, int leaderCommit) throws RemoteException {
		Response response = null;

		if (term == CLIENT_REQUEST) { // FROM CLIENT

			if (server.getState() != ServerState.LEADER) { // NOT LEADER
				System.err.println("Redirecting to Leader (" + leaderID + ")" );
				response = new Response(-1, false);
				response.setLeaderID(leaderID);

			} else {  // LEADER
				if (entries != null && entries.length != 0) {
					LogEntry lastLog = logHandler.getLastLogEntry();
					LogEntry lastCommitedLog = logHandler.getLastCommitedLogEntry();

					if (lastCommitedLog != null) {
						lastLog.setLastCommitedIndex(lastCommitedLog.getLogIndex());
					}
					eh.setLastLog(lastLog);

					int[] indexes2Commit = logHandler.writeLogEntries(entries, eh.getTerm());

					// adiciona as entries ah queue para serem despachadas quando tiverem tempo de processamento
					for (int i = 0; i < entries.length; i++) {
						entryQueue.add(entries[i]);
					}

					List<Server> servers = raftServers.getServers();
					int quorum = (servers.size() / 2);

					Queue<Response> responseQueue = server.getResponseQueue();

					int voteSuccess = 0;
					int totalVoteCount = 0;

					System.out.println(entries[0].getClientID() + " says '" + entries[0].getEntry() + "'");

					// Stores the entries to remove them from the responseQueue
					List<Response> elements = new ArrayList<>();

					//por cada entry vai percorrer a queue ah procura das respostas dessa entry
					while (totalVoteCount < quorum && (voteSuccess < quorum)) {  
						for (Response element : responseQueue) {

							if (element.isLogDeprecated()) {
								element.resetLogDeprecated();
								elements.add(element);

								// If a log is deprecated, continues until is synchronized
								handleDeprecatedLog(element, leaderId, servers);
								continue;
							}

							if (element.getRequestID().equals(entries[0].getRequestID())) {
								if (element.isSuccessOrVoteGranted()) {
									voteSuccess++;
								}
								totalVoteCount++;
								elements.add(element);
							}
						}
					}

					// Removes the received responses from the current request
					for (Response e: elements) {
						responseQueue.remove(e);
					}

					if (voteSuccess >= quorum) {
						response = handleSuccessAppendEntry(indexes2Commit, entries);
					} else {	
						response = new Response(eh.getTerm(), false);
					}

				} else {
					response = new Response(eh.getTerm(), true);
				}
			}

			// Commits a log in all servers
		} else if (term == COMMIT_LOG) {
			for (Entry entry: entries) {
				String command = logHandler.commitLogEntry(entry.getCommitedIndex());

				System.out.println("COMMIT_LOG " + command);

				stateMachine.execute(command);
			}

			// When a follower receives a request to appendEntries
		} else {  

			if (eh.getTerm() < term) {
				eh.setTerm(term);
			}

			leaderID = leaderId;

			if (entries != null && entries.length != 0) {
				response = logHandler.followerReplication(term, 
						prevLogIndex, prevLogTerm, entries, leaderCommit);
			}

			if (server.getState() != ServerState.FOLLOWER){
				eh.setServerState(ServerState.FOLLOWER);
				serverThreadPool.interruptThreads();
				serverThreadPool.purgeQueues();
			}

			eh.resetState();
		}

		if (response != null && response.getLeaderID() == 0){
			response.setLeaderID(server.getServerID());
		}

		return response;
	}

	/**
	 * Executes the commands in the state machine
	 * @return output of the commands back to the client
	 */
	private Response handleSuccessAppendEntry(int[] indexes2Commit, Entry[] entries) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < entries.length; i++) {
			sb.append(stateMachine.execute(entries[i].getEntry()) + (i + 1 < entries.length ? "\n" : ""));
		}

		// Only commits if the response is successful
		for (int i: indexes2Commit) {
			logHandler.commitLogEntry(i);
			commitQueue.add(new Entry(i));
		}

		return new Response(eh.getTerm(), sb.toString());
	}

	/**
	 * Sends a request to a server that is deprecated
	 * with the updated logs
	 */
	private void handleDeprecatedLog(Response e, int leaderId, List<Server> servers) {
		List<LogEntry> logEntry = logHandler.getAllEntriesAfterIndex(e.getLastLogIndex());

		Entry [] entriesOfLog = new Entry[logEntry.size()];
		for (int i = 0; i < entriesOfLog.length; i++) {
			entriesOfLog[i] = logEntry.get(i).convertToEntry();
		}

		try {
			AppendEntriesRequest ar = new AppendEntriesRequest(eh.getTerm(), leaderId,
					e.getLastLogIndex(), e.getLastLogTerm(), entriesOfLog, 0);

			servers.get(e.getServerID() - 1).getRequestQueue().put(ar);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
}
