package model;
import client.Client;
import gui.GroupChatController;
import gui.MainChatController;
import gui.UpdateGroupChat;
import gui.UpdateMainChat;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class AddedGroup {
	private Client client;
	private String groupName;
	private SimpleIntegerProperty numberOfMembers;
	private Button button;
	private int groupId;
	
	public AddedGroup(String groupName, int numberOfMembers, int groupId) {
		this.groupName = groupName;
		this.numberOfMembers = new SimpleIntegerProperty(numberOfMembers);
		this.button = new Button("Chat");
		this.groupId = groupId;
		button.setOnAction(e -> {
			this.beginChat(e);
		});
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Integer getNumberOfMembers() {
		return numberOfMembers.get();
	}

	public void setNumberOfMembers(SimpleIntegerProperty numberOfMembers) {
		this.numberOfMembers = numberOfMembers;
	}

	public Button getButton() {
		return button;
	}

	public void setButton(Button button) {
		this.button = button;
	}

	
	public void beginChat(ActionEvent e) {
		Stage stage = new Stage();
		try {
			client.updateAccountProfile2Controller.hideWindow();
			UpdateGroupChat update = new UpdateGroupChat(client,groupId,client.user.getUsername());
			new GroupChatController(update);
			this.client.updateGroupChat = update;
			client.getGroupMessages(groupId, client.user.getUsername());
		
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
		
}
