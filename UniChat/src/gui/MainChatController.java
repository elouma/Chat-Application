package gui;
import client.Client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainChatController extends Application {
	
	private Client client;
	private FXMLLoader loader;
	private UpdateMainChat updateMainChat;

	public MainChatController(UpdateMainChat updateMainChat){
    	client = updateMainChat.client;
    	loader = updateMainChat.loader;
    	this.updateMainChat = updateMainChat;
    	try {
			start(updateMainChat.stage);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	    
    @Override
    public void start(Stage stage) throws Exception {
    	try {
    		
			Parent root = loader.load();
			stage.setTitle("Main Chat");
			Scene scene = new Scene(root);
			stage.setScene(scene);
			updateMainChat.setRecipientProfile();
			updateMainChat.setConversationName();
			stage.show();	
			updateMainChat.findGroupId(updateMainChat.recipientUsername, client.user.getUsername());
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
