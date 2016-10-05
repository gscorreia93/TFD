package client;

import java.util.Scanner;
import java.util.UUID;

import client.library.*;

public class Client {

	public static void main(String[] args) {
		
		final String clientID = UUID.randomUUID().toString();
		
		ClientLibrary c = new ClientLibrary();
		
		Scanner leitor = new Scanner(System.in);
		int option = 0;
		int x = 0;
		int y = 0;
		
		while(true){
			
			Menu();
			
			option = leitor.nextInt();
			
			if( option == 3 ){
				System.out.println("Good bye :)");
				break;
			}
			
			System.out.print("Digit X: ");
			x = leitor.nextInt();
			System.out.print("Digit Y: ");
			y = leitor.nextInt();
			
			if (option == 1){
				c.request(clientID, 8081, x+"+"+y);
			}
			else{
				if (option == 2){
					c.request(clientID, 8081, x+"-"+y);
				}
				else{
					if (option > 3 || option < 1){
						System.out.println("Invalid command. Try again");
					}
				}
			}
		}

	}
	
	public static void Menu (){
		System.out.println("--- CLIENT MENU ---");
		System.out.print("1. x+y\n2. x-y\n3. exit\nOption >");		
	}

}
