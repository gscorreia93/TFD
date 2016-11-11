package server.library;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import exceptions.ServerNotFoundException;
import server.library.log.LogHandler;
import server.library.log.LogReplication;

public class ServerHandler extends UnicastRemoteObject implements RemoteMethods {

	private static final long serialVersionUID = 1L;

	public static final int COMMIT_LOG = -2;
	private final int CLIENT_REQUEST = -1;

	private LogHandler lh;
	private RAFTServers raftServers;

	private Server server;
	private ElectionHandler eh;
	private ServerThreadPool serverThreadPool;

	private int leaderID;

	public ServerHandler() throws RemoteException {
		super();

		raftServers = new RAFTServers();
	}

	public void openConnection() {
		server = startServer(raftServers.getServers());

		if (server != null) {

			List<Server> servers = raftServers.getServers();
			serverThreadPool = new ServerThreadPool(servers.size());

			serverThreadPool.startThreads(servers);

			eh = new ElectionHandler(server, raftServers, serverThreadPool);
			eh.startElectionHandler();

			// Starts the log handler
			lh = new LogHandler("LOG_" + server.getPort());

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
System.out.println("\tServer " + candidateID + " request on term " + term + " (current " + eh.getTerm() + ")");

		Response response = new Response(eh.getTerm(), false);

		if (term > eh.getTerm()) {
			eh.setTerm(term);
			eh.resetState();
			eh.setVoted(true);

			serverThreadPool.interruptThreads();
			serverThreadPool.purgeQueues();

			if (!eh.isFollower() && candidateID != server.getServerID()) {
System.out.println("SOU FOLLOWER!!! no term " + term + "  e voto no " + candidateID);
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

		if (term == CLIENT_REQUEST) { // If it is a Client Request
			if(server.getState() != ServerState.LEADER) {
				System.out.println("Redirecting to Leader (" + leaderID + ")" );
				response = new Response(-1, false);
				response.setLeaderID(leaderID);
			} else {
				response = new LogReplication(server, raftServers.getServers(), lh).leaderReplication(entries, eh.getTerm());
			}

			// Commits a log in all servers
		} else if (term == COMMIT_LOG) {
			response = new LogReplication(server, raftServers.getServers(), lh).commitLog(leaderCommit, eh.getTerm());


			// When a follower receives a request to appendEntries
		} else if (entries != null && server.getState() != ServerState.LEADER) {

			response = new LogReplication(server, raftServers.getServers(), lh).followerReplication(term,
					leaderID, prevLogIndex, prevLogTerm, entries, leaderCommit, eh.getTerm());


			// When a heartbeat is received
		} else if (entries == null && server.getState() != ServerState.LEADER) {
			leaderID = leaderId;
			if (server.getState() != ServerState.FOLLOWER){
				eh.setServerState(ServerState.FOLLOWER);

				serverThreadPool.interruptThreads();
				serverThreadPool.purgeQueues();
			}

			eh.resetState();
		}

		return response;
	}
}
