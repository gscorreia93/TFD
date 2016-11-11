package server.library;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public class ServerThreadPool {

	private ServerThread[] threadPool;

	public ServerThreadPool(int size) {
		threadPool = new ServerThread[size];
	}

	protected void startThreads(List<Server> servers) {
		int counter = 0;
		for (Server sv : servers) {
			ServerThread thread = new ServerThread(sv);
			thread.start();
			threadPool[counter] = thread;
			counter++;
		}
	}

	protected void interruptThreads() {

		for (int i = 0; i < threadPool.length; i++) {
			if (threadPool[i] != null) {
				threadPool[i].interrupt();
			}
		}
	}

	protected void purgeQueues() {

		for (int i = 0; i < threadPool.length; i++) {
			if (threadPool[i] != null) {
				threadPool[i].purgeQueues();
			}
		}
	}

	/**
	 * Keeps a thread running for a server
	 */
	public class ServerThread extends Thread {

		private BlockingQueue<Request> requestQueue;
		private Queue<Response> responseQueue;
		private Queue<Response> voteQueue;
		private Server server;
		private boolean interrupted;

		public ServerThread(Server server) {
			this.requestQueue = server.getRequestQueue();
			this.responseQueue = server.getResponseQueue();
			this.voteQueue = server.getVoteQueue();
			this.server = server;
			this.interrupted = false;
		}

		private void purgeQueues() {

			requestQueue.clear();
			responseQueue.clear();
			voteQueue.clear();
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
				} catch (Exception e) {
				}
			}

			Request rq = null;

			while (!interrupted) { // Keeps checking the requestQueue to see if
									// there are more requests
				try {
					if (rq == null)
						rq = requestQueue.take();

					if (rq.getClass() == RequestVoteRequest.class) {

						RequestVoteRequest typedRequest = (RequestVoteRequest) rq;

						try {
							Response response = stub.requestVote(typedRequest.getTerm(), typedRequest.getServerId(),
									typedRequest.getLastLogIndex(), typedRequest.getLastLogTerm());

							synchronized (response) {
System.out.println("\t" + server.getPort() + " voting " + response.isSuccessOrVoteGranted() + " to " + typedRequest.getServerId());
								voteQueue.add(response);
							}

						} catch (RemoteException e) {
							System.err.println("RequestVoteRequest: Connection failed, retrying...");
							break;
						}

					} else if (rq.getClass() == AppendEntriesRequest.class) {

						AppendEntriesRequest typedRequest = (AppendEntriesRequest) rq;

						try {
							Response response = stub.appendEntries(typedRequest.getTerm(), typedRequest.getServerId(),
									typedRequest.getLastLogIndex(), typedRequest.getLastLogTerm(),
									typedRequest.getEntries(), typedRequest.getLeaderCommit());

							if (response != null) {
								// To replicate a log
								synchronized (response) {
									responseQueue.add(response);
								}
							}

						} catch (RemoteException e) {
							System.err.println("AppendEntriesRequest: Connection failed, retrying...");
							break;
						}
					}

					rq = null;

				} catch (InterruptedException e) {
					interrupted = true;
					break;
				} // eof try
			} // eof while (true)

		} // eof run
	} // eof class ServerThread
}
