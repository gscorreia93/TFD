package _old;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerThread extends Thread {
	private Socket socket = null;
	private String utilizador = null;


	public ServerThread(Socket clientSocket) {
		socket = clientSocket;
	}


	public void run(){
		try {
			ObjectOutputStream out= new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

			utilizador = (String) in.readObject();
			String messagemRecebida = "";
			while(messagemRecebida.compareTo("exit")!=0){
				messagemRecebida = (String) in.readObject();
				System.out.println(utilizador+": "+messagemRecebida );
			}
			
			System.out.println(utilizador+" disconnected");

			out.close();
			in.close();
			socket.close();

		} catch (IOException e){
			System.out.println("No answer from user");
			System.out.println(utilizador+" disconnected");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}



	
}






