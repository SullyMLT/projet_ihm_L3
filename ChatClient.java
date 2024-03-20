import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;

public class ChatClient {
	
	Socket socket;
	ObjectOutputStream output;
	ObjectInputStream input;
	
	ChatController chatController;
	
	boolean estConnecte;
	
	public ChatClient(ChatController c) {
		this.chatController = c;
		estConnecte = false;
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
				estConnecte = true;
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
	    if (socket == null || socket.isClosed()) {
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
	
	public boolean isConnected() {
		return this.estConnecte;
	}
	
	public void deconnexion() {
		if (socket != null) {
			try {
				chatController.changeEtatConnection(false);
				estConnecte = false;
				this.output.writeObject(new String("chaine"));
				socket.close();
				socket = null;
				System.out.println("Déconnecté");
			} catch (IOException e) {
				System.out.print("Le stream ne marche plus");	
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
				this.input = new ObjectInputStream(socket.getInputStream());
			} catch (IOException e) {
				System.out.print("Problème lors de la création de l'inputStream");
				e.printStackTrace();
			}
		}
		
		public void run() {

			while(socket.isConnected()) {
				System.out.print("En Attente d'un message \n");
				try {
					String message = (String)input.readObject();
					if (message == null) {
			            break; // Sortir de la boucle si le serveur se déconnecte
			        }
					this.chatClient.chatController.ajouterMessage(message + "\n");
					
				} catch (ClassNotFoundException | IOException e) {
					break;
				}
			}
		}
		
		
	}
	
	
}