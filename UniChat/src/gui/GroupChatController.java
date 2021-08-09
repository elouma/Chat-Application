package gui;
import client.Client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GroupChatController extends Application {
	
	private Client client;
	private FXMLLoader loader;
	private UpdateGroupChat updateGroupChat;

	public GroupChatController(UpdateGroupChat updateGroupChat){
    	client = updateGroupChat.client;
    	loader = updateGroupChat.loader;
    	this.updateGroupChat = updateGroupChat;
    	try {
			start(updateGroupChat.stage);
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
			updateGroupChat.setConversationName();
			stage.show();	
//			updateGroupChat.findGroupId(updateGroupChat.recipientUsername, client.user.getUsername());
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
