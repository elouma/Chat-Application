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
import model.AddedGroup;
import model.Group;

public class UpdateAccountProfile2Controller {
	Client client;
	public FXMLLoader loader;
	public Stage stage;
	
	@FXML private MenuButton addButton;	
	@FXML private Button changeToFriend;
	@FXML private Button logoutButton;
	
	@FXML private TableView<AddedGroup> groupTable;
	@FXML private TableColumn<AddedGroup, String> groupName;
	@FXML private TableColumn<AddedGroup, Integer> numberOfMembers;
	@FXML private TableColumn<AddedGroup, Button> button;
	
	public ObservableList<AddedGroup> groups;
	public List<AddedGroup> groupList = new ArrayList<>();
	
	public UpdateAccountProfile2Controller(Client client) {
		this.client = client;
		loader = new FXMLLoader(getClass().getResource("../AccountProfile2.fxml"));
		loader.setController(this);
		this.stage = new Stage();
	}
	
	public void updateTable() {
		groupTable.getItems().clear();
		groups = FXCollections.observableArrayList(groupList);
		groupName.setCellValueFactory(new PropertyValueFactory<AddedGroup, String> ("groupName"));
		numberOfMembers.setCellValueFactory(new PropertyValueFactory<AddedGroup, Integer> ("numberOfMembers"));
		button.setCellValueFactory(new PropertyValueFactory<AddedGroup, Button>("button") );
		groupTable.setItems(groups);
	}
	
	public void passGroup(List<AddedGroup> response) {
		for(AddedGroup eachRow : response) {
			eachRow.getButton().setText("Chat");
			groupList.add(eachRow);
		}
		updateTable();
		groupList.clear();
	}	
	
	public void addFriends(ActionEvent e) throws IOException {
		UpdateAddFriends update = new UpdateAddFriends(client);
		new AddFriendsController(update);
		client.updateAddFriends = update;
		addButton.getScene().getWindow().hide();
	}
	
	public void addGroups(ActionEvent e) throws IOException {
		UpdateAddGroups update = new UpdateAddGroups(client);
		new AddGroupsController(update);
		client.updateAddGroups = update;
		addButton.getScene().getWindow().hide();		
	}
	
	public void createGroup(ActionEvent e) {
		// jump into create group page
	}
	
	public void changeToFriend(ActionEvent e) throws IOException {
		UpdateAccountProfileController update = new UpdateAccountProfileController(client);
		new AccountProfileController(update);
		client.updateAccountProfileController = update;
		changeToFriend.getScene().getWindow().hide();
		client.findFriends();
	}
	
	public void logout(ActionEvent e) throws IOException {
		LoginController.client = client;
		Stage stage = new Stage();
		Parent root;
		root = FXMLLoader.load(getClass().getResource("../Login.fxml"));
		Scene scene = new Scene(root);
		stage.setTitle("Login");
		stage.setScene(scene); 
		stage.show();
		addButton.getScene().getWindow().hide();
	}
	
	public void passClient(Client client) {
		AccountProfile2Controller.client = client;
	}
	
	public void hideWindow() {
		addButton.getScene().getWindow().hide();
	}

}
