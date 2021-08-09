package client;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import gui.LoginController;
import gui.UpdateAccountProfile2Controller;
import gui.UpdateAccountProfileController;
import gui.UpdateAddFriends;
import gui.UpdateAddGroups;
import gui.UpdateGroupChat;
import gui.UpdateMainChat;
import javafx.application.Application;
import model.AddedGroup;
import model.Friend;
import model.Group;
import model.User;

/**
 * A client class implementation for Unichat
 * 
 * @author Rumit Mehta
 * @author Hang Shi-just the signup part
 */
public class Client {

	public Client client;
	public User user;
	public Friend friend;
	public UpdateAddFriends updateAddFriends;
	public UpdateAddGroups updateAddGroups;
	public UpdateAccountProfileController updateAccountProfileController;
	public UpdateAccountProfile2Controller updateAccountProfile2Controller;
	public UpdateMainChat updateMainChat;
	public UpdateGroupChat updateGroupChat;
	public boolean isLoggedIn = false;
	public static ObjectOutputStream ClientOut;
	public static ObjectInputStream ClientIn;
	public Socket socket;
	public static final int MAX_THREADS = 10;
	public boolean transfer;
	public int groupNumber;
	public boolean signedUp;

	

	/**
	 * Constructor for client
	 * 
	 * @param host IP address of host
	 * @param port port number that server is listening to
	 */
	public Client(String host, int port) {

		// socket created on client side with BOTH host IP address and port
		try {
			socket = new Socket(host, port);
			ClientIn = new ObjectInputStream(socket.getInputStream());
			ClientOut = new ObjectOutputStream(socket.getOutputStream());
			System.out.println("Connected: " + socket.getLocalPort());
			ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREADS);

			threadPool.execute(new ServerHandler());

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("connection with server lost");
		}
	}
	
	
	/**
	 * Method that sends a string array to the server in the format
	 * [firstname, lastname, username, password, email, degree, project, photo] and recieves a boolean from the server
	 * indicating if the client successfully logged in
	 * 
	 * @param firstname  the clients firstname from SignupController
	 * @param lastname  the clients lastname from SignupController
	 * @param username  the clients username from SignupController
	 * @param password  the clients password from SignupController
	 * @param email  the clients email from SignupController
	 * @param degree  the clients degree from SignupController
	 * @param project  the clients project from SignupController
	 * @param photo  the clients photo from SignupController
	 */
	public boolean signup(String firstname,
			                     String lastname,
			                     String username,
								 String password,
								 String email,
								 String degree,
								 String project,
								 File photo) throws UnsupportedEncodingException {

		System.out.println("sending necessary information to server");
		System.out.println(photo.getClass());
		signedUp = false;
//		byte[] buffer = new byte[(int) photo.length()];
//		try {
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
//			ObjectOutputStream sOut = new ObjectOutputStream(out);
//			FileInputStream fis = new FileInputStream(photo);
//			fis.read(buffer);
//			fis.close();
//			buffer = out.toByteArray();
//			sOut.writeObject(photo);
//			sOut.close();
//			out.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		System.out.println(photo.length());
//
//		String photoS = new String(buffer, "UTF-8");
		String filePath = "";
		try {
			filePath = photo.toURI().toURL().toString();
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String[] request = new String[] {"Signup", firstname, lastname, username, password,
				email, degree, project, filePath};

		try {
			ClientOut.writeObject(request);
			ClientOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		synchronized (this){
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return signedUp;
	}

	
	public byte[] fileToByte(File img) {
		
    	byte[] bytes = null;
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			BufferedImage bi;
			bi = ImageIO.read(img);
			ImageIO.write(bi, "png", baos);
			bytes = baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				baos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bytes;
	}
	
	
	/**
	 * Method that sends a string array to the server in the format
	 * ["Login",username, password] and recieves a boolean from the server
	 * indicating if the client successfully logged in
	 * 
	 * @param username the clients username from LoginController
	 * @param password the clients password from LoginController
	 * @return boolean Sends a boolean to LoginController to allow the client to
	 *         login or not
	 */
	public boolean login(String username, String password) {
		System.out.println("sending username and password to server");
		String[] request = new String[] { "Login", username, password };

		try {
			ClientOut.writeObject(request);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		synchronized (this){
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("end of login method");
		System.out.println(isLoggedIn);
		
		if(isLoggedIn) {
			getUserInfo(username,password);
		}
		
		return isLoggedIn;
	}
	
	public void getUserInfo(String username, String password) {
		String[] request = new String[] { "UserInfo", username, password };
		try {
			ClientOut.writeObject(request);
		} catch (IOException e) {
			e.printStackTrace();
		}
		synchronized (this){
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void retrivePicture(String actualUsername) {
		String[] request = new String[] {"Retrive Picture", actualUsername};
		try {
			ClientOut.writeObject(request);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(String message, String clientUsername ,String recipientUsername) {
		System.out.println("Sending message to server");
		String[] request = new String[] {"Send 1-1 Message", message, clientUsername, recipientUsername, user.getFirstname() + " " + user.getSurname()};
		try {
			ClientOut.writeObject(request);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void getMessages(String clientUsername, String recipientUsername) {
		String[] request = new String[] {"getMessage", recipientUsername, clientUsername, user.getFirstname() + " " + user.getSurname()};
		System.out.println("leaving Client.getMessages");
		try {
			ClientOut.writeObject(request);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Method that logs the client out of the
	 */
	public void close() {
		System.out.println("Logging out");
		String[] request = new String[] { "Close" };
		try {
			ClientOut.writeObject(request);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void createGroup(String[] usernames, String groupName) {
		System.out.println("sending create group message to server");
		//for the case with 2 people
		if(groupName == null) {
			groupName = usernames[0] + " & " + usernames[1];
		}
		List<String> listRequest = new ArrayList<>();
		listRequest.add("CreateGroup");
		listRequest.add(groupName);
		for(String user : usernames) {
			listRequest.add(user);
		}
		String[] request = new String[listRequest.size()];
		for(int i = 0; i < request.length; i++) {
			request[i] = listRequest.get(i);
		}
		
		try {
			ClientOut.writeObject(request);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void searchUsers(String[] searchConditions) {
		System.out.println("sending search conditions to server");
		String[] request = searchConditions;
		try {
			ClientOut.writeObject(request);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void searchGroups(String[] searchConditions) {
		System.out.println("sending search conditions to server");
		String[] request = searchConditions;
		try {
			ClientOut.writeObject(request);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void startConversation(String recipientUsername) {
		System.out.println("sending target username to server to start conversation");		

		try {
			String[] request = new String[] {"StartConversation",recipientUsername,user.getUsername()};
			ClientOut.writeObject(request);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addGroup(String groupName) {
		System.out.println("sending target group to server to add the group");	
		try {
			String[] request = new String[] {"AddGroup", groupName, user.getUsername()};
			ClientOut.writeObject(request);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void findFriends() {
		String request[] = new String[] {"findFriends",user.getUsername()};
		try {
			ClientOut.writeObject(request);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void findGroups() {
		System.out.println("Inside Client findGroups");
		String request[] = new String[] {"findGroups",user.getUsername()};
		try {
			ClientOut.writeObject(request);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void findGroupId(String recipientUsername, String clientUsername) {
		String request[] = new String[] {"findGroupId",clientUsername, recipientUsername};
		try {
			ClientOut.writeObject(request);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String host = "localhost";
		int port = 50000;
		Client client = new Client(host, port);
		new LoginController().passClient(client);
		Application.launch(LoginController.class, new String[] {});
	}
	
	public void getGroupMessages(int groupId, String username) {
		// TODO Auto-generated method stub
		
	}
	
	//---------------------------------------------------------------------------------------------

	class ServerHandler implements Runnable {

		@Override
		public void run() {
			try {
				while (true) {
						System.out.println("serverHandler listening for requests from server");
						String[] request = (String[]) ClientIn.readObject();
						sortRequest(request);

				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// TODO Auto-generated method stub

		}

	}

	//---------------------------------------------------------------------------------------------

	public void sortRequest(String[] request) {
		switch (request[0]) {
			case "LoginResponse":
				isLoggedIn = Boolean.parseBoolean(request[1]);
				System.out.println("SORT REQUEST WORKS " + isLoggedIn);
				synchronized(this) {
					this.notify();
				}
				break;
			case "searchUserResponse":
				handleSearchUserResponse(request);
				break;
			case "searchGroupResponse":
				handleSearchGroupResponse(request);
			case "userInfoResponse":
				this.user = new User(request[1], request[2], request[3], request[4], request[5], request[6], request[7]);
				System.out.println(user.toString());
				synchronized(this) {
					this.notify();
				}
				break;
			case "findFriendsResponse":
				System.out.println("in the server handler");
				handleFindFriendResponse(request);
				break;
			case "findGroupsResponse":
				System.out.println("in the server handler");
				handleFindGroupResponse(request);
				break;
			case "gotMessage":
				handleGotMessageResponse(request);
				break;
			case "send 1-1 Message":
				System.out.println("Group number: " + groupNumber + " " + request[3]);
				if(groupNumber == Integer.parseInt(request[3])) {
					updateMainChat.showRecieveMessage(request[2], request[4]); // -------->>
				}
				break;
			case "GroupIdResponse":
				System.out.println("Sending groupId to gui");
				groupNumber = Integer.parseInt(request[1]);
				System.out.println("this is the groupNumber:" + groupNumber);
				break;
			case "signedUp":
				signedUp = Integer.parseInt(request[1]) > 0;
				System.out.println("signedUp is " + signedUp);
				synchronized(this) {
					this.notify();
				}
				break;
			case "RetrivePictureResponse":
					updateMainChat.passPicture(request[1]);
				break;
			default:
				break;
		}
	}
	/**
	 * 
	 * @param request {gotMessage, recipient firstname+lastname , username, message 1, username, message 2, username, message 3, ... }
	 */
	public void handleGotMessageResponse(String[] request) {
		System.out.println("inside handleGotMessageResponse");
		if(updateMainChat != null) {
			System.out.println("d;nkf'sdnf'sfj;sdfhn;shn");
			for(int i = 2; i < request.length; i=i+2) {
				System.out.println(request[i]);
				if(request[i].equals(user.getUsername())) {
					updateMainChat.showSendMessage(request[i+1], user.getFirstname() + " " + user.getSurname());
				} else {
					updateMainChat.showRecieveMessage(request[i+1], request[1]);
				}
			}
		}	
	}
	
	
	public void handleFindFriendResponse(String[] request){
		List<Friend> response = new ArrayList<Friend>();
		for(int i = 1; i <request.length; i = i + 4) {
			Friend friend = new Friend(request[i],request[i+1],request[i+2],request[i+3],this);
			response.add(friend);
		}
		if(request.length > 1) {
			updateAccountProfileController.passUser(response);
		} else {
			System.out.println("User has no contacts");
		}
		System.out.println("sending friends list to GUI");
		
	}
	
	/**
	 * 
	 * @param request {"findGroupsResponse",group name, num of group members, groupId ... repeats}
	 */
	public void handleFindGroupResponse(String[] request) {
		List<AddedGroup> response = new ArrayList<AddedGroup>();
		for(int i = 1; i <request.length; i = i + 3) {
			AddedGroup group = new AddedGroup(request[i], Integer.valueOf(request[i+1]), Integer.valueOf(request[i+2]));
			response.add(group);
		}
		if(request.length > 1) {
			updateAccountProfile2Controller.passGroup(response);
		} else {
			System.out.println("User has no group");
		}
		System.out.println("sending friends list to GUI");
	}
	
	public void handleSearchUserResponse(String[] request) {
		List<User> response = new ArrayList<User>();
		System.out.println(request.length);
		for(int i = 1; i <request.length; i = i + 4) {
			User user = new User(request[i],request[i+1],request[i+2],request[i+3],this);
			response.add(user);
		}
		updateAddFriends.passUser(response);
		
	}
	
	public void handleSearchGroupResponse(String[] request) {
		List<Group> response = new ArrayList<Group>();
		System.out.println(request.length);
		for(int i = 1; i <request.length; i = i + 2) {
			Group group = new Group(request[i],Integer.valueOf(request[i+1]),Integer.valueOf(request[i+2]));
			response.add(group);
		}
		updateAddGroups.passGroup(response);
	}
	
}
