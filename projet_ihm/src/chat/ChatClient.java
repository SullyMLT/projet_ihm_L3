package chat;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;

import ui.ChatController;

public class ChatClient {
	
	Socket socket;
	ObjectOutputStream output;
	
	ChatController chatController;
	
	public ChatClient(ChatController c) {
		this.chatController = c;
	}
	
	public Socket getSocketChatClient() {
		return this.socket;
	}
	
	public void connectToServer(String addr, int port) {
		try {
			if (socket == null) {
				socket = new Socket(addr, port);
				this.output = new ObjectOutputStream(this.socket.getOutputStream());
				String lecture = "Débloquage";
				output.writeObject(lecture);
				new ThreadClient(socket, this).start();
				chatController.changeEtatConnection(true);
			}else {
				chatController.afficherPopupErreur("Vous êtes déjà connecté");
			}
		} catch (IOException e) {
			if (e instanceof ConnectException && e.getMessage().contains("Connection refused")) {
	            chatController.afficherPopupErreur("L'adresse du serveur ou le Port non valide !");
	        } else {
	            chatController.afficherPopupErreur("Erreur de la connexion au serveur !");
	        }
	        //e.printStackTrace();
		}
	}
	
	public int envoyerMessage(String message) {
	    if (socket == null/* || socket.isClosed()*/) {
	        System.out.println("La socket est fermée");
	        return 0;
	    }
	    
	    try {
	        if (this.output == null) {
	            this.output = new ObjectOutputStream(this.socket.getOutputStream());
	            System.out.println("Connexion du stream");
	        }
	        System.out.println("Envoie du message au serveur");
	        output.writeObject(message);
	        return 1;
	    } catch (IOException e) {
	        System.out.println("Erreur lors de l'envoi du message.");
	        e.printStackTrace();
	    }
	    return 0;
	}
	
	
	public void deconnexion() {
		/*System.out.println(
				socket.isBound()
				+ " " + socket.isClosed());*/
		if (/*socket.isConnected() && */socket != null) {
			try {
				socket.close();
				chatController.changeEtatConnection(false);
				socket = null;
				System.out.println("Déconnecté");
			} catch (IOException e) {
				System.out.print("La socket n'est pas connecté");
				e.printStackTrace();
			}
		}else {
			chatController.afficherPopupErreur("Vous êtes déjà déconnecté de tout serveur !");
		}
	}
	
	private class ThreadClient extends Thread {
		
		Socket socket;
		ObjectInputStream input;
		ChatClient chatClient;
		
		public ThreadClient(Socket socket, ChatClient cc) {
			this.socket = socket;
			this.chatClient = cc;
			try {
				input = new ObjectInputStream(socket.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void run() {

			int passage = 0;
			while(socket.isConnected() && !socket.isInputShutdown() && passage < 10) {
				passage++;
				System.out.print("En Attente d'un message \n");
				try {
					String message = (String)input.readObject();
					if (message == null) {
			            break; // Sortir de la boucle si le serveur se déconnecte
			        }
					this.chatClient.chatController.ajouterMessage(message + "\n");
					
				} catch (ClassNotFoundException | IOException e) {
					System.out.print("Plus d'accès au serveur \n");
				}
			}
		}
		
		
	}
	
	
}
