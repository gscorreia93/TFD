package _old;



import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

	private static Socket clientSocket;
	private static ObjectOutputStream out ;
	private static ObjectInputStream in;


	private static boolean connect (String server, int port){
		boolean connected=false;
		try {
			clientSocket = new Socket (server,port); 
			out = new ObjectOutputStream (clientSocket.getOutputStream());
			in = new ObjectInputStream (clientSocket.getInputStream());
			connected=true;
		} catch (UnknownHostException e){  
			System.out.println(server+" not found!");
		}catch(IOException e) { 
			System.out.println(server+" maybe is not online.");

		}
		return connected;
	}

	public static void disconnect(){
		try {
			in.close();
			out.close();
			clientSocket.close();
			System.out.println("Cliente desligado!");
		} catch (IOException e) {
			System.out.println("Cliente nao foi desligado. Tente novamente!");
		}
	}

	public static void main(String[]args) throws InterruptedException, IOException {
		
		String mensagemEnviar="";
		Scanner leitor = new Scanner(System.in);
		
		if(connect("localhost", 1234)){ 

			System.out.print("Username:");
			String nome = leitor.nextLine();
			out.writeObject(nome);
			
			while(mensagemEnviar.compareTo("exit")!=0){
				System.out.print(">");
				mensagemEnviar = leitor.nextLine();
				out.writeObject(mensagemEnviar);
			}

			disconnect();
		}
	}
}