package ui;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import chat.ChatClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ChatController {
	
	ChatClient chatClient;
	
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextArea areaDiscussion;

    @FXML
    private TextField entreeAdresseIP;

    @FXML
    private TextField entreeMessage;

    @FXML
    private TextField entreePort;

    @FXML
    private TextField entreePseudo;

    @FXML
    private Label labelEtatConnexion;
    
    public void changeEtatConnection(boolean connect) {
    	if (connect) {
    		labelEtatConnexion.setText("Connecté");
    		afficherPopupInformation("Vous êtes connecté !");
    	}else {
    		labelEtatConnexion.setText("Déconnecté");
    		afficherPopupInformation("Vous venez de vous déconnecter !");
    	}
    }
    
    @FXML
    void actionBoutonConnexion(ActionEvent event) {
    	//Erreur à tester
    	String addr = this.entreeAdresseIP.getText();
    	int port = Integer.parseInt(this.entreePort.getText());
    	
    	chatClient.connectToServer(addr, port);
    }

    @FXML
    void actionBoutonDeconnexion(ActionEvent event) {
    	chatClient.deconnexion();
    }

    @FXML
    void actionBoutonEnvoyer(ActionEvent event) {
    	if (chatClient.getSocketChatClient() != null) {
	    	String pseudo = this.entreePseudo.getText();
	    	
	    	if (!(pseudo.equals(""))) {
	    		
	    		LocalTime heureActuelle = LocalTime.now();
	        	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
	        	String heureMinuteSeconde = heureActuelle.format(formatter);
	        	
	        	String message = this.entreeMessage.getText();
	        	if (message.equals("")) {
	        		afficherPopupErreur("Veuillez écrire un message avant de cliquer sur envoyé");
	        		return;
	        	}
	        	chatClient.envoyerMessage(new String("["+ heureMinuteSeconde +"] "+pseudo + " : " + message));
	        	entreeMessage.clear();
	    	}else {
	    		afficherPopupErreur("Veuillez rentrer un pseudo !");
	    	}
	    	
    	}else {
    		afficherPopupErreur("Veuillez-vous connecter !");
    	}
    	
    }
    
    public void ajouterMessage(String message) {
    	areaDiscussion.appendText(message);
    }
    
    private void afficherPopup(String message, AlertType type) {
        Alert alert = new Alert(type);
        if (type == AlertType.ERROR) {
          alert.setTitle("Erreur");
        } else {
          alert.setTitle("Information");
        }
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.setResizable(true);
        alert.showAndWait();
      }
      
      public void afficherPopupErreur(String message) {
        this.afficherPopup(message, AlertType.ERROR);
      }
      
      public void afficherPopupInformation(String message) {
        this.afficherPopup(message, AlertType.INFORMATION);
      }
    
    @FXML
    void initialize() {
        chatClient = new ChatClient(this);
        this.entreeAdresseIP.setText("localhost");
        this.entreePort.setText("5000");
    }

}
