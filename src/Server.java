import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

enum Status{
	Leader,
	Follower,
	Candidate
}

public class Server {

	private ServerSocket server;
	private Status status;
	private StringBuilder logFile;
	
	public Server (int porta){

		try {
			server = new ServerSocket (porta); 
			status = Status.Follower;
			logFile = new StringBuilder();
		}catch (IOException e){
			System.out.println("Server not started");
			System.exit(-1);
		}
		System.out.println("Server started at "+porta);

	}

	private ServerSocket getServer(){
		return server;
	}

	public static void main (String [] args) {

		Server servidor = new Server(1234);

		while(true){
			Socket clientSocket = null;
			try {
				clientSocket = servidor.getServer().accept();
			} catch (IOException e) {
				System.out.println("Server did not accepted");
			}
			System.out.println("A user has connected");
			ServerThread client = new ServerThread (clientSocket);
			new Thread(client).start();
		}	
	}
}
