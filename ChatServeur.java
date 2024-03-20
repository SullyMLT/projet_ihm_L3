import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServeur {
	
	//La socket du serveur
	ServerSocket serverSocket;

	List<ThreadServer> threads = new ArrayList<ThreadServer>();
	
	public void demarrerServeur(int port) {
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Démarrage serveur");
			// En attente de connexion
			while (true) {
		        
				Socket socket = serverSocket.accept();
				System.out.print("connexion ! \n");
				ThreadServer ts = new ThreadServer(socket, this);
				threads.add(ts);
				ts.start();
			}
		} catch (IOException e) {
			System.out.print("Problème lors du démarrage du serveur \n");
			e.printStackTrace();
		}
	}
	
	
	public void redistribuerMessage(Object message, ThreadServer tsSender) {
		
		for (ThreadServer ts : threads) {
			ts.envoyerMessage(message);				
		}
		System.out.println("Envoie d'un message sur tous les threads : " + message);
		System.out.println("Nombre de threads : " + threads.size());
	}
	
	/**
	 * la class ThreadServer qui est appelé dès qu'un client se connecte
	 * Elle hérite de thread et qui permet de recevoir et d'envoyer les messages
	 * envoyé par le client à l'aide d'ObjectInputStream et d'ObjectOutputStream
	 */
	private class ThreadServer extends Thread{
		
		Socket socket; //Socket avec le client
		ObjectInputStream input; //Le stream d'input
		ObjectOutputStream output; //Le stream d'output
		ChatServeur parentInstance; //Référence au serveur
		
		/**
		 * Constructeur
		 * @param socket le socket relié au client
		 * @param parentInstance l'instance relié au parent
		 */
		public ThreadServer(Socket socket, ChatServeur parentInstance) {
			this.socket = socket;
			this.parentInstance = parentInstance;
			try {
	            this.output = new ObjectOutputStream(socket.getOutputStream());
	            this.input = new ObjectInputStream(socket.getInputStream());
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
		
		
		public void envoyerMessage(Object message) {
			try {
		        if (!socket.isClosed()) { // Vérifier si la socket est ouverte
		            output.writeObject(message);
		            output.flush();
		        } else {
		            System.out.println("La socket est fermée. Impossible d'envoyer le message.");
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}
		

		@Override
		public void run() {
		    try {
		    	//DEBLOQUE LE TRUC 
		    	//RECUPERE LE PREMIER MESSAGE
		    	Object firstLecture = input.readObject();
		        while (socket.isConnected()) {
		            // Lire les données de manière asynchrone
		            Object receivedData = input.readObject();
		            
		            if (receivedData.equals("chaine")){
		            	break;
		            }
		            // Traiter les données reçues ici...
		            System.out.println("Données reçues : " + receivedData);
		            
		            // Redistribue le message à tous les threads actifs
		            this.parentInstance.redistribuerMessage(receivedData, this);
		        }
		        System.out.print("Fermeture du thread \n");
		        
		        //Fermeture de tous les components
		        input.close();
		        output.close();
		        socket.close();
		        //On retire le thread de la liste
		        this.parentInstance.threads.remove(this);
		    } catch (IOException | ClassNotFoundException e) {
		    	//Unreachable
		        e.printStackTrace();
		    }
		    
		}
	}
	
	public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java ServeurMonoClient <port>");
            return;
        }
        
        ChatServeur instance = new ChatServeur();
        
        instance.demarrerServeur(Integer.parseInt(args[0]));
	}
}