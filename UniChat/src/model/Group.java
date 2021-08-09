package model;
import client.Client;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class Group {
	private Client client;
	private String groupName;
	private int groupId;
	private SimpleIntegerProperty numberOfMembers;
	private Button button;
	
	public Group(String groupName, int numberOfMembers, int groupId) {
		this.groupId = groupId;
		this.groupName = groupName;
		this.numberOfMembers = new SimpleIntegerProperty(numberOfMembers);
		this.button = new Button("Add");
		button.setOnAction(e -> {
			this.addGroup(e);
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

	
	public void addGroup(ActionEvent e) {
		Stage stage = new Stage();
			try {
				client.addGroup(getGroupName());
				button.setDisable(true);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	
}
