package server;

import java.rmi.RemoteException;

import server.library.ServerHandler;

public class MainServer {

	public static void main(String[] args) throws RemoteException {
		ServerHandler sh = new ServerHandler();
		sh.openConnection();
	}
}
