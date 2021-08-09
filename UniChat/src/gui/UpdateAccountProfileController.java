package gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import client.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Friend;

/**
 * 
 * @author Rumit Mehta
 *
 */
public class UpdateAccountProfileController {
	Client client;
	public FXMLLoader loader;
	public Stage stage;
	
	@FXML private MenuButton addButton;
	@FXML private Button changeToGroup;
	@FXML private Button logoutButton;
	
	@FXML private TableView<Friend> userTable;
	@FXML private TableColumn<Friend, String> Firstname;
	@FXML private TableColumn<Friend, String> Surname;
	@FXML private TableColumn<Friend, String> Username;
	@FXML private TableColumn<Friend, String> courseName;
	@FXML private TableColumn<Friend, Button> button;
	
	public ObservableList<Friend> users; 
	public List<Friend> friendList = new ArrayList<>();
	
	public UpdateAccountProfileController(Client client) {
		this.client = client;
		loader = new FXMLLoader(getClass().getResource("../AccountProfile.fxml"));
		loader.setController(this);
		this.stage = new Stage();
	}

	public void updateTable() {
		userTable.getItems().clear();
		users = FXCollections.observableArrayList(friendList);
		Firstname.setCellValueFactory(new PropertyValueFactory<Friend, String> ("Firstname"));
		Surname.setCellValueFactory(new PropertyValueFactory<Friend, String> ("Surname"));
		Username.setCellValueFactory(new PropertyValueFactory<Friend, String> ("username"));
		courseName.setCellValueFactory(new PropertyValueFactory<Friend, String> ("courseName"));
		button.setCellValueFactory(new PropertyValueFactory<Friend, Button>("button") );
		userTable.setItems(users);
		
	}
	
	public void createGroup(ActionEvent e) {
		// jump into create group page
	}
	
	public void addFriends(ActionEvent e) throws IOException {
		UpdateAddFriends update = new UpdateAddFriends(client);
		new AddFriendsController(update);
		client.updateAddFriends = update;
		
	}
	
	public void addGroups(ActionEvent e) throws IOException {
		UpdateAddGroups update = new UpdateAddGroups(client);
		new AddGroupsController(update);
		client.updateAddGroups = update;
		addButton.getScene().getWindow().hide();		
	}
	
	public void changeToGroup(ActionEvent e) throws IOException {
		UpdateAccountProfile2Controller update2 = new UpdateAccountProfile2Controller(client);
		new AccountProfile2Controller(update2);
		this.client.updateAccountProfile2Controller = update2;	
		changeToGroup.getScene().getWindow().hide();
		client.findGroups();
		
	}
	
	public void logout(ActionEvent e) throws IOException {
		LoginController.client = client;
		Stage stage = new Stage();
		LoginController.stage = stage;
		Parent root;
		root = FXMLLoader.load(getClass().getResource("../Login.fxml"));
		Scene scene = new Scene(root);
		stage.setTitle("Login");
		stage.setScene(scene); 
		stage.show();
		addButton.getScene().getWindow().hide();
	}

	public void passUser(List<Friend> response) {
		for(Friend eachRow : response) {
			eachRow.getButton().setText("Chat");
			friendList.add(eachRow);
		}
		updateTable();
		friendList.clear();
	}	
	
	public void hideWindow() {
		addButton.getScene().getWindow().hide();
	}
}
