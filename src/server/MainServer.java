package server;

import server.library.ServerHandler;

public class MainServer {

	public static void main(String[] args) {
		
		ServerHandler sh = new ServerHandler();
		sh.openConnection();
	}

}
