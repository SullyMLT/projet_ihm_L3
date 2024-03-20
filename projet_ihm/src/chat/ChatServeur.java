package chat;
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
			/*if (ts.socketfermer()) {
				ts.interrupt();
				threads.remove(ts);
			}else {
				ts.envoyerMessage(message);				
			}*/
			ts.envoyerMessage(message);
		}
		System.out.println("Envoie d'un message sur tous les threads : " + message);
		System.out.println("Nombre de threads : " + threads.size());
	}
	
	
	private class ThreadServer extends Thread{
		Socket socket; //Socket avec le client
		ObjectInputStream input;
		ObjectOutputStream output;
		ChatServeur parentInstance;
		
		
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
		        	//////// =========== TESTTTT ============
		        	/*for (ThreadServer ts : parentInstance.threads) {
		    			if (ts.socketfermer()) {
		    				ts.interrupt();
		    				threads.remove(ts);
		    			}
		    		}*/
		            System.out.println("La socket est fermée. Impossible d'envoyer le message.");
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}
		
		private boolean socketfermer() {
		    return socket.isClosed();
		}

		@Override
		public void run() {
		    try {
		    	//DEBLOQUE LE TRUC 
		    	//RECUPERE LE PREMIER MESSAGE
		    	Object firstLecture = input.readObject();
	            // Traiter les données reçues ici...
	            System.out.println("Données reçues : " + firstLecture);
		        while (socket.isConnected()) {
		            // Lire les données de manière asynchrone
		            Object receivedData = input.readObject();
		            
		            // Si l'object receivedData est null alors on sort de la boucle (dans le cas de la déconnexion)
		            if (receivedData == null) {
		                break;
		            }
		            // Traiter les données reçues ici...
		            System.out.println("Données reçues : " + receivedData);
		            
		            // Simuler un traitement de données en attente
		            Thread.sleep(1000);
		            
		            this.parentInstance.redistribuerMessage(receivedData, this);
		            // Écrire des données de manière asynchrone
		            // Par exemple, vous pouvez avoir un mécanisme de réponse
		         
		            
		            // Simuler un traitement d'envoi de données
		            Thread.sleep(1000);
		        }
		        System.out.print("Fermeture du thread \n");
		        
		        input.close();
		        output.close();
		        socket.close();
		    } catch (IOException | ClassNotFoundException | InterruptedException e) {
		        e.printStackTrace();
		    } finally {
		        // Fermer les flux et la connexion
		        try {
		            input.close();
		            output.close();
		            socket.close();
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
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
