package gui;

import client.Client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AddFriendsController extends Application{

	static Client client;
	FXMLLoader loader;

    public AddFriendsController(UpdateAddFriends updateAddFriends){
    	client = updateAddFriends.client;
    	loader = updateAddFriends.loader;
    	try {
			start(updateAddFriends.stage);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	@Override
	public void start(Stage arg0) throws Exception {
		Stage stage = new Stage();
		Parent root = loader.load();
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("../AddFriends.css").toExternalForm());
		stage.setTitle("Add Friends");
		stage.setScene(scene);
		stage.show();
		
	}

}
