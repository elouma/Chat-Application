package server;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;


/**
 * A simple (multi-threaded) server implementation
 * 
 * @author Jian Tang
 * @author Rumit Mehta
 * @author Hang Shi
 *
 */
public class Server {
	@SuppressWarnings("unused")
	private boolean loggedin;
	public static final int MAX_THREADS = 10;
	
	Map<String, ClientHandlerThread> clientMap = new HashMap<String, ClientHandlerThread>(); //<username, socket>
	

	/**
	 * Constructor for the ServerLogin class
	 * 
	 * @param port port number to listen on
	 */
	public Server(int port) {
		
		// initialise ServerSocket object that will be used to accept new clients
		try (ServerSocket serverSocket = new ServerSocket(port)) {

			System.out.println("Creating server listening " + "to port " + port);

			// initialise a fixed size thread pool that can allow up to MAX_THREADS
			// concurrent threads
			ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREADS);

			// an infinite loop to accept clients indefinitely (on the main thread)
			while (true) { 
				// System.out.println("Waiting for client to connect");
				// call .accept to wait for a new client to connect
				// a new socket object is returned by .accept when the
				// new client connects successfully
				Socket newClientSocket = serverSocket.accept();
				System.out.println("connected to client with port:" + newClientSocket.getPort());
				
				// pass the socket created for the new client to a separate
				// ClientHandlerThread object and execute it on the thread pool 
				threadPool.execute(new ClientHandlerThread(newClientSocket));
			}

		}

		catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Inner class to represent a dedicated task that handles a particular client
	 * 
	 * @author Jian Tang
	 * @author Rumit Mehta
	 */
	private class ClientHandlerThread implements Runnable {
		private ObjectInputStream serverIn;
		private ObjectOutputStream serverOut;
		private Socket clientSocket;
		boolean connected;

		/**
		 * Simple constructor that takes the socket created by the ServerSocket.accept()
		 * method
		 * 
		 * @param clientSocket the socket created by serverSocket.accept() that
		 *                     communicates to a specific client
		 */
		public ClientHandlerThread(Socket clientSocket) {
			this.clientSocket = clientSocket;
			connected = true;
		}

		/*
		 * every thing that happens inside the run method will execute on a new thread
		 */
		@Override
		public void run() {
			try { 
				serverOut = new ObjectOutputStream(clientSocket.getOutputStream());
				serverIn = new ObjectInputStream(clientSocket.getInputStream());
				
				while (connected) {
					String[] request = (String[]) serverIn.readObject();
					sortRequest(request);
				}
				
				clientSocket.close();
			} catch (IOException e) {
				System.out.println("Connection with client lost.");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		/**
		 * The sortRequest method sorts Looks at the request from the client and figures out what method to call.
		 * 
		 * @param request a string request where the first term dictates the type of the request
		 */
		public void sortRequest(String[] request) {
			if (request[0].equals("Login")) {
				System.out.println("server received login request");
				handleLogin(request);
			} else if (request[0].equals("Close")) {
				System.out.println("server received Close request");
				handleLogout();
			} else if (request[0].equals("Signup")) {
				handleSignup(request);
			} else if (request[0].equals("Send 1-1 Message")) {
				handleMessage(request[1],request[2],request[3],request[4]);
			} else if (request[0].equals("SearchUsers")) {
				handleSearchUsers(request);
			} else if (request[0].equals("SearchGroups")) {
				handleSearchGroups(request);
			} else if (request[0].equals("StartConversation")) {
				handleStartConversation(request[1],request[2]);
			} else if (request[0].equals("AddGroup")){
				handleAddGroup(request[1],request[2]);
			} else if (request[0].equals("UserInfo")) {
				handleUserInfo(request[1],request[2]);
			} else if (request[0].equals("findFriends")) {
				handleFindFriends(request);
			} else if (request[0].equals("findGroups")) {
				handleFindGroups(request);
			} else if (request[0].equals("CreateGroup")) {
				handleCreateGroup(request);
			} else if (request[0].equals("getMessage")) {
				handleGetMessage(request[1],request[2]);
			} else if (request[0].equals("findGroupId")) {
				handleFindGroupId(request[1],request[2]);
			} else if (request[0].equals("Retrive Picture")) {
				handleRetrivePicture(request[1]);
			}
				return;
		}		
		
		
		
		/**
		 * The handleLogin method handles the login request from the clients.
		 * 
		 * @param request the request from the client in the format: ["Login", username,
		 *                password]
		 */
		public void handleLogin(String[] request) {
			String sql = "SELECT USER_NAME, USER_PASSWORD FROM USER_INFO WHERE USER_NAME = '" + request[1] + "' AND USER_PASSWORD = '" + request[2] + "'";
			try(Connection connection = connectDatabase(); 	
				Statement statement = connection.createStatement()) {
				
				ResultSet resultSet = statement.executeQuery(sql);
				//System.out.println(Boolean.toString(resultSet.next()));
				String[] response = new String[] {"LoginResponse",Boolean.toString(resultSet.next())};
				if(response[1] == "true") {
					clientMap.put(request[1],this);
				}
				System.out.print(response[0] + response[1]);
				serverOut.writeObject(response);
				System.out.print(response[0] + response[1]);
				serverOut.flush();
				System.out.print(response[0] + response[1]);
				
			} catch (Exception e) {
				System.out.println(sql);
				e.printStackTrace();
			}
			
		}
		
		public void handleUserInfo(String username,  String password) {
			String sql = "SELECT * FROM USER_INFO WHERE USER_NAME = '" + username + "' AND USER_PASSWORD = '" + password + "'" ;
			
			try(Connection connection = connectDatabase(); 	
					Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
					ResultSet resultSet = statement.executeQuery(sql);
					String[] response = new String[8];
					response[0] = "userInfoResponse";
					while(resultSet.next()) {
						response[1] = resultSet.getString(1); // Firstname
						response[2] = resultSet.getString(2); // Surname
						response[3] = resultSet.getString(3); // Username
						response[4] = resultSet.getString(4); // password
						response[5] = resultSet.getString(5); // email
						response[6] = resultSet.getString(6); // course type
						response[7] = resultSet.getString(7); // course name
						
					}
					
					System.out.println("Sending response to serverhandler");
					serverOut.writeObject(response);
					serverOut.flush();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		
		
		/**
		 * The handleSignup method handles the handle request from the clients.
		 * 
		 * @param request the request from the client 
		 * @author Hang Shi
		 * @author Jian Tang
		 */
		public void handleSignup(String[] request) {
			
			
			
			String sql = "INSERT INTO user_Info (first_name,last_name,user_name,user_password,email,course_type,course_name,create_date,photo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			try(Connection connection = connectDatabase();
					PreparedStatement statement = connection.prepareStatement(sql)) {
					statement.setString(1, request[1]);
					statement.setString(2, request[2]);
					statement.setString(3, request[3]);
					statement.setString(4, request[4]);
					statement.setString(5, request[5]);
					statement.setString(6, request[6]);
					statement.setString(7, request[7]);
					statement.setDate(8, Date.valueOf(LocalDate.now()));
					// statement.setBytes(9, request[8].getBytes());		
					System.out.println(request[8].substring(5));
					try {
						File file = new File(request[8].substring(5));
						FileInputStream fis = new FileInputStream(file);
						statement.setBinaryStream(9, fis, (int)file.length());
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					int flag = statement.executeUpdate();
					String[] response = new String[] {"signedUp", String.valueOf(flag)};
					serverOut.writeObject(response);
					serverOut.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		
		
		public void handleRetrivePicture(String actualUsername) {
			String[] names = actualUsername.split(" ");
			String sql = "SELECT PHOTO FROM USER_INFO WHERE first_name = ? AND last_name = ?";
			
			byte[] bytes = null;
			try(Connection connection = connectDatabase(); 
					PreparedStatement statement = connection.prepareStatement(sql);){
			
			statement.setString(1, names[0]);
			statement.setString(2, names[1]);
			ResultSet resultSet = statement.executeQuery();
			
			String[] response = new String[2];
			response[0] = "RetrivePictureResponse";
			
			while(resultSet.next()) {	
				bytes = resultSet.getBytes(1);
				ByteArrayInputStream in = new ByteArrayInputStream(bytes);
				System.out.println("From pass picture" + Arrays.toString(bytes));
		        BufferedImage bufferedImage =ImageIO.read(in); 
		        try {   
		            File file = new File("res/" + actualUsername + ".png");
		            if(!file.exists()){
		            	try {
		            		file.createNewFile();
		            	} catch (IOException e) {
		            		e.printStackTrace();
		            	}
		            }
		            
		            ImageIO.write(bufferedImage, "png", file);
		        } catch (IOException e) {   
		            e.printStackTrace();   
		        }
					        
			    response[1] = "res/" + actualUsername + ".png";	 
			}
			
			System.out.println("Sending response to serverhandler");
			serverOut.writeObject(response);
			serverOut.flush();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
					
		
		}
		
		
		
		public void handleFindFriends(String[] request) {
			String sql = "SELECT USER_ID FROM USER_INFO WHERE USER_NAME = '" + request[1] + "'";
			String sql2;
			String sql3;
			int clientId;
			int contact_userId;
			try(Connection connection = connectDatabase(); 	
					Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
					Statement statement2 = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
					Statement statement3 = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)
							) {
					ResultSet resultSet = statement.executeQuery(sql);
				
					if(resultSet.next()) {
						clientId = resultSet.getInt(1);
						sql2 = "SELECT USER2_ID FROM USER_CONTACT WHERE USER1_ID = '" + clientId + "'";
						ResultSet resultSet2 = statement.executeQuery(sql2);
						int counter = 0;
						
						// to find length of response
						while(resultSet2.next()) {
							counter++;
						}
						
						resultSet2.beforeFirst();
						String[] response = new String[(4 * counter) + 1];
						System.out.println(response.length);
						response[0] = "findFriendsResponse";
						int[] contact_userIds = new int[counter ];
						
						counter = 0;
						while(resultSet2.next()) {
							contact_userIds[counter] = resultSet2.getInt(1);
							counter++;
						}
						
						counter = 0;
						for(int contact_id : contact_userIds) {
							sql3 = "SELECT * FROM USER_INFO WHERE USER_ID = " + contact_id;
							ResultSet resultSet3 = statement.executeQuery(sql3);
							if(resultSet3.next()) {
								response[counter + 1] = resultSet3.getString(1); // Firstname
								response[counter + 2] = resultSet3.getString(2); // Surname
								response[counter + 3] = resultSet3.getString(3); // Username							
								response[counter + 4] = resultSet3.getString(7); // course name
								counter = counter + 4;
							}
						
						}		
									
						serverOut.writeObject(response);
						serverOut.flush();						
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		
		public void handleFindGroups(String[] request) {
			String sql = "SELECT GROUP_NAME, COUNT(ui.user_id) NUMBER_OF_MEMBERS, gi.group_id FROM USER_INFO ui JOIN "
					+ "USER_GROUP ug ON ui.user_id = ug.user_id JOIN GROUP_INFO gi ON ug.group_id = "
					+ "gi.group_id WHERE GROUP_NAME IN (SELECT DISTINCT GROUP_NAME FROM GROUP_INFO "
					+ "JOIN user_group ug on group_info.group_id = ug.group_id JOIN user_info ui "
					+ "on ug.user_id = ui.user_id WHERE ui.user_name = '" + request[1] + "') GROUP BY gi.group_name,gi.group_id";
			System.out.println(sql);
			try(Connection connection = connectDatabase(); 	
					Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
					ResultSet resultSet = statement.executeQuery(sql);
					int counter = 0;
					while(resultSet.next()) {
						counter++;
					}
					
					resultSet.beforeFirst();
					String[] response = new String[(3 * counter) + 1];
					response[0] = "findGroupsResponse";
					
					counter = 0;
					while(resultSet.next()) {
						response[counter+1] = resultSet.getString(1); 
						response[counter+2] = String.valueOf(resultSet.getInt(2));
						response[counter+3] = String.valueOf(resultSet.getInt(3));
						counter = counter + 3;
					}
					
					System.out.println("Sending response to serverhandler");
					serverOut.writeObject(response);
					serverOut.flush();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			
		}

		public void handleMessage(String message, String username ,String recipeintUsername, String senderName) {
			System.out.println("in server handleMessage method");			
			String sql1 = "SELECT USER_ID FROM USER_INFO WHERE USER_NAME = '" + username + "'";
			String sql2 = "SELECT USER_ID FROM USER_INFO WHERE USER_NAME = '" + recipeintUsername + "'";
			String sql4 = "INSERT INTO MESSAGE_INFO (GROUP_ID, USER_ID, CREATE_DATE, MESSAGE_BODY) VALUES(?,?,?,?)";
			try(Connection connection = connectDatabase(); 	
				Statement statement = connection.createStatement();
				Statement statement2 = connection.createStatement();
				Statement statement3 = connection.createStatement();
				PreparedStatement statement4 = connection.prepareStatement(sql4)){	
				int userId = 0;
				int recipientId = 0;
				int groupId = 0;
				
				ResultSet resultSet = statement.executeQuery(sql1);
				if(resultSet.next()) {
					userId = resultSet.getInt(1);
				}
				ResultSet resultSet2 = statement2.executeQuery(sql2);

				if(resultSet2.next()) {
					recipientId = resultSet2.getInt(1);
				}
				
				System.out.println(userId + " " + recipientId);
				String sql3 = "SELECT GROUP_ID, COUNT(GROUP_ID) FROM " +		
						"(SELECT * FROM USER_GROUP AS A JOIN " +
						"(SELECT COUNT(GROUP_ID) AS numInGroup, GROUP_ID AS GROUP_ID2 FROM USER_GROUP GROUP BY GROUP_ID2) AS B " +
						"ON A.GROUP_ID = B.GROUP_ID2 " +
						"WHERE ((A.USER_ID = " + userId + " OR A.USER_ID = "+ recipientId +" ) AND numInGroup = 2) " +
						") AS C GROUP BY GROUP_ID HAVING COUNT(GROUP_ID) = 2 ";
				
				System.out.println(sql3);
				ResultSet resultSet3 = statement3.executeQuery(sql3);
				if(resultSet3.next()) {
					groupId = resultSet3.getInt(1);
				}
				
				statement4.setInt(1, groupId);
				statement4.setInt(2, userId);
				statement4.setDate(3,Date.valueOf(LocalDate.now()));
				statement4.setString(4, message);		
				statement4.executeUpdate();		
				
				for(Iterator<String> iterator = clientMap.keySet().iterator(); iterator.hasNext();) {
					System.out.println(iterator.next());	
				}
				System.out.println(recipeintUsername);
				if(clientMap.containsKey(recipeintUsername)) {
					System.out.println("send message back to different client: " + recipeintUsername);
					clientMap.get(recipeintUsername).serverOut.writeObject(new String[]{"send 1-1 Message", username, message, Integer.toString(groupId), senderName});
					clientMap.get(recipeintUsername).serverOut.flush();
				}
				
				serverOut.flush();
				
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		
		public void handleGetMessage(String recipientUsername, String clientUsername) {
			System.out.println("entering server.handleGetMessage");
			String sql1 = "SELECT USER_ID, FIRST_NAME, LAST_NAME FROM USER_INFO WHERE USER_NAME = '" + clientUsername + "'";
			String sql2 = "SELECT USER_ID, FIRST_NAME, LAST_NAME FROM USER_INFO WHERE USER_NAME = '" + recipientUsername + "'";

			try(Connection connection = connectDatabase(); 	
				Statement statement = connection.createStatement();
				Statement statement2 = connection.createStatement();
				Statement statement3 = connection.createStatement();
				Statement statement4 = connection.createStatement()){	
				int userId = 0;
				int recipientId = 0;
				String recipientFirstName = "";
				String recipientLastName = "";
				int groupId = 0;
				List<String> messages = new ArrayList<>();
				messages.add("gotMessage");
				
				ResultSet resultSet = statement.executeQuery(sql1);
				if(resultSet.next()) {
					userId = resultSet.getInt(1);
					recipientFirstName = resultSet.getString(2);
					recipientLastName = resultSet.getString(3);
					}
				
				ResultSet resultSet2 = statement2.executeQuery(sql2);
				if(resultSet2.next()) {
					recipientId = resultSet2.getInt(1);
				}
				messages.add(recipientFirstName + " " + recipientLastName);
				System.out.println(userId + " " + recipientId);
				String sql3 = "SELECT GROUP_ID, COUNT(GROUP_ID) FROM " +		
						"(SELECT * FROM USER_GROUP AS A JOIN " +
						"(SELECT COUNT(GROUP_ID) AS numInGroup, GROUP_ID AS GROUP_ID2 FROM USER_GROUP GROUP BY GROUP_ID2) AS B " +
						"ON A.GROUP_ID = B.GROUP_ID2 " +
						"WHERE ((A.USER_ID = " + userId + " OR A.USER_ID = "+ recipientId +" ) AND numInGroup = 2) " +
						") AS C GROUP BY GROUP_ID HAVING COUNT(GROUP_ID) = 2 ";
				
				System.out.println(sql3);
				ResultSet resultSet3 = statement3.executeQuery(sql3);
				if(resultSet3.next()) {
					groupId = resultSet3.getInt(1);
				}
				String sql4 = "SELECT USER_INFO.USER_NAME, MESSAGE_INFO.MESSAGE_BODY FROM MESSAGE_INFO JOIN USER_INFO ON MESSAGE_INFO.USER_ID = USER_INFO.USER_ID WHERE MESSAGE_INFO.GROUP_ID ="  + groupId;
				ResultSet resultSet4 = statement4.executeQuery(sql4);
				while(resultSet4.next()) {
					messages.add(resultSet4.getString(1));
					messages.add(resultSet4.getString(2));
				}
				
				serverOut.writeObject(messages.toArray(new String[] {}));
				
				serverOut.flush();
					
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("leaving server.handleGetMessage");
		}

		
		
		/**
		 * 
		 * @param request {"searchUsers",firstname, surname, course, email, client_username}
		 */
		public void handleSearchUsers(String[] request) {
			for(int i = 0; i < request.length - 1; i++) { // request length - 1 because request[5] should stay the same
				String word = request[i];
				if(word.isEmpty()) {
					word = "IS NOT NULL";
				} else {
					word = "ILIKE '" + word + "%'";
				}
				request[i] = word;
			}
			String sql = "SELECT * FROM USER_INFO WHERE FIRST_NAME " + request[1] + " AND LAST_NAME " + request[2] + " AND COURSE_NAME " + request[3] + " AND USER_NAME " + request[4];
			System.out.println(sql);
	
			try(Connection connection = connectDatabase(); 	
					Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
					ResultSet resultSet = statement.executeQuery(sql);
					int counter = 0;
					while(resultSet.next()) {
						if(!resultSet.getString(3).equals(request[5]) && !isContact(request[5],resultSet.getString(3))) {
							counter++;
						}
						
					}
					resultSet.beforeFirst();
					String[] response = new String[(4 * counter) + 1];
					response[0] = "searchUserResponse";
					counter = 0;
					while(resultSet.next()) {
						if(!resultSet.getString(3).equals(request[5]) && !isContact(request[5],resultSet.getString(3))) {
							response[counter+1] = resultSet.getString(1); //firstname
							response[counter+2] = resultSet.getString(2); //surname
							response[counter+3] = resultSet.getString(3); //username
							response[counter+4] = resultSet.getString(6); //coursename
							counter = counter + 4;
						}
					}
					
					System.out.println("Sending response to serverhandler");
					serverOut.writeObject(response);
					serverOut.flush();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		
		
		public boolean isContact(String userUsername, String recipientUsername) {
			String sql1 = "SELECT * FROM USER_INFO WHERE USER_NAME =  '" + recipientUsername + "'";
			String sql2 = "SELECT * FROM USER_INFO WHERE USER_NAME =  '" + userUsername + "'";
			int recipient_userId = -1;
			int user_userId = -1;
			try(Connection connection = connectDatabase(); 	
					Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
					ResultSet resultSet = statement.executeQuery(sql1);
					while(resultSet.next()) {
						recipient_userId = Integer.parseInt(resultSet.getString(11)); 
					}
					resultSet = statement.executeQuery(sql2);
					while(resultSet.next()) {
						user_userId = Integer.parseInt(resultSet.getString(11)); 
					}
					String sql3 = "SELECT * FROM USER_CONTACT WHERE USER1_ID = '" + user_userId + "' AND USER2_ID = '" + recipient_userId + "'" ;
					resultSet = statement.executeQuery(sql3);
					return resultSet.next(); 
				} catch (Exception e) {
					e.printStackTrace();
				}
			return false; //incase the connection fails
		}
		
		/**
		 * 
		 * @param request {"searchGroups",groupName, groupMember, username}
		 */
		public void handleSearchGroups(String[] request) {	
			String sql = "SELECT GROUP_NAME, COUNT(ui.user_id) NUMBER_OF_MEMBERS, gi.GROUP_ID FROM USER_INFO ui "
					+ "JOIN USER_GROUP ug ON ui.user_id = ug.user_id JOIN GROUP_INFO gi ON "
					+ "ug.group_id = gi.group_id WHERE GROUP_NAME ILIKE '%" + request[1] +"%' AND ui.user_name "
					+ "ILIKE '%" + request[2] + "%'  AND GROUP_NAME NOT IN (SELECT DISTINCT GROUP_NAME FROM GROUP_INFO "
							+ "JOIN user_group ug on group_info.group_id = ug.group_id JOIN user_info ui on ug.user_id = "
							+ "ui.user_id WHERE ui.user_name = '" + request[3] + "')\n" + "GROUP BY gi.group_name,gi.GROUP_ID";
			
			System.out.println(sql);
	
			try(Connection connection = connectDatabase(); 	
					Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
					ResultSet resultSet = statement.executeQuery(sql);
					int counter = 0;
					while(resultSet.next()) {
						counter++;
					}
					
					resultSet.beforeFirst();
					String[] response = new String[(3 * counter) + 1];
					response[0] = "searchGroupResponse";
					
					counter = 0;
					while(resultSet.next()) {
						response[counter+1] = resultSet.getString(1); 
						response[counter+2] = String.valueOf(resultSet.getInt(2)); 
						response[counter+3] = String.valueOf(resultSet.getInt(2)); 
						counter = counter + 3;
					}
					
					System.out.println("Sending response to serverhandler");
					serverOut.writeObject(response);
					serverOut.flush();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		
		
		/**
		 * This method severs the connection between the client and server.
		 */
		public void handleLogout() {
			System.out.println("Client disconnected");
			loggedin = false;
		}
		
		public void handleAddGroup(String groupName, String username) {
			String sql1 = "SELECT GROUP_ID FROM GROUP_INFO WHERE GROUP_NAME = '"+ groupName +"'";
			String sql2 = "SELECT USER_ID FROM USER_INFO WHERE USER_NAME = '" + username + "'";
			int groupID = -1;
			int userID = -1;
			try(Connection connection = connectDatabase(); 	
					Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
					ResultSet resultSet = statement.executeQuery(sql1);
					while(resultSet.next()) {
						groupID = Integer.parseInt(resultSet.getString(1)); 
					}
					
					resultSet = statement.executeQuery(sql2);
					while(resultSet.next()) {
						userID = Integer.parseInt(resultSet.getString(1)); 
					}
					updateUserGroupTable(groupID, userID);
				} catch (Exception e) {
					e.printStackTrace();
				}
			
		}
		
		public void updateUserGroupTable(int groupID, int userID) {
			String sql = "INSERT INTO USER_GROUP (USER_ID, GROUP_ID, UG_CREATE_DATE) VALUES (?, ?, ?)";
			try(Connection connection = connectDatabase();
					PreparedStatement statement = connection.prepareStatement(sql)) {
					statement.setInt(1, userID );
					statement.setInt(2, groupID);
					statement.setDate(3,Date.valueOf(LocalDate.now()));
					statement.executeUpdate();	
					serverOut.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}

		public void handleStartConversation(String recipientUsername, String userUsername) {
			System.out.println("Adding user contact into database");
			String sql1 = "SELECT * FROM USER_INFO WHERE USER_NAME =  '" + recipientUsername + "'";
			String sql2 = "SELECT * FROM USER_INFO WHERE USER_NAME =  '" + userUsername + "'";
			int recipient_userId = -1;
			int user_userId = -1;
			try(Connection connection = connectDatabase(); 	
					Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
					ResultSet resultSet = statement.executeQuery(sql1);
					while(resultSet.next()) {
						recipient_userId = Integer.parseInt(resultSet.getString(11)); 
					}
					resultSet = statement.executeQuery(sql2);
					while(resultSet.next()) {
						user_userId = Integer.parseInt(resultSet.getString(11)); 
					}
					updateUserContactTable(recipient_userId, user_userId);
				} catch (Exception e) {
					e.printStackTrace();
				}

		}
		
		public void updateUserContactTable(int recipient_userId, int user_userId) {
			String sql =  "INSERT INTO USER_CONTACT (user1_id, user2_id,uc_create_date) VALUES (?, ?, ?)";
			
			try(Connection connection = connectDatabase();
					PreparedStatement statement = connection.prepareStatement(sql)) {
					statement.setInt(1, user_userId );
					statement.setInt(2, recipient_userId);
					statement.setDate(3,Date.valueOf(LocalDate.now()));
					statement.executeUpdate();
					statement.setInt(1, recipient_userId);
					statement.setInt(2, user_userId );
					statement.setDate(3, Date.valueOf(LocalDate.now()));
					statement.executeUpdate();
					serverOut.flush();
					System.out.println("i am here");
				} catch (Exception e) {
					e.printStackTrace();
				}	
		}
		
		public void handleFindGroupId(String clientUsername, String recipientUsername) {
			String sql1 = "SELECT USER_ID, FIRST_NAME, LAST_NAME FROM USER_INFO WHERE USER_NAME = '" + clientUsername + "' OR USER_NAME = '" + recipientUsername + "'";
			
			try(Connection connection = connectDatabase(); 	
				Statement statement = connection.createStatement();
				Statement statement3 = connection.createStatement();
				Statement statement4 = connection.createStatement()){	
				int userId = 0;
				int recipientId = 0;
				String recipientFirstName = "";
				String recipientLastName = "";
				int groupId = 0;
				List<String> messages = new ArrayList<>();
				messages.add("gotMessage");
				
				ResultSet resultSet = statement.executeQuery(sql1);
				if(resultSet.next()) {
					userId = resultSet.getInt(1);
				}
				if(resultSet.next()) {
					recipientId = resultSet.getInt(1);
					recipientFirstName = resultSet.getString(2);
					recipientLastName = resultSet.getString(3);
				}
				messages.add(recipientFirstName + " " + recipientLastName );
				System.out.println(userId + " " + recipientId);
				String sql3 = "SELECT GROUP_ID, COUNT(GROUP_ID) FROM " +		
						"(SELECT * FROM USER_GROUP AS A JOIN " +
						"(SELECT COUNT(GROUP_ID) AS numInGroup, GROUP_ID AS GROUP_ID2 FROM USER_GROUP GROUP BY GROUP_ID2) AS B " +
						"ON A.GROUP_ID = B.GROUP_ID2 " +
						"WHERE ((A.USER_ID = " + userId + " OR A.USER_ID = "+ recipientId +" ) AND numInGroup = 2) " +
						") AS C GROUP BY GROUP_ID HAVING COUNT(GROUP_ID) = 2 ";
				
				System.out.println(sql3);
				ResultSet resultSet3 = statement3.executeQuery(sql3);
				if(resultSet3.next()) {
					groupId = resultSet3.getInt(1);
				}
				
				System.out.println(groupId);
				
				String[] response = new String[] {"GroupIdResponse", Integer.toString(groupId)};
				serverOut.writeObject(response);
				serverOut.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		//added a method that can add information to the group table
		public void handleCreateGroup(String[] request) {
			String sql = "INSERT INTO GROUP_INFO (GROUP_NAME, GROUP_CREATE_DATE) VALUES (?, ?)";
			String sql2 = "SELECT GROUP_ID FROM GROUP_INFO WHERE GROUP_NAME = '" + request[1] + "'"; 
			String sql3;
			List<Integer> groupId = new ArrayList<>();
			int userId;
			try(Connection connection = connectDatabase();
					PreparedStatement statement = connection.prepareStatement(sql);
					Statement statement2 = connection.createStatement()){
					statement.setString(1, request[1]);
					statement.setDate(2,Date.valueOf(LocalDate.now()));
					statement.executeUpdate();
					ResultSet resultSet = statement2.executeQuery(sql2);
					while(resultSet.next()) {
						groupId.add(resultSet.getInt(1));
					}
					
					// starts from 2 because first 2 elements aren't usernames
					for(int i = 2; i < request.length; i++) {
						sql3 = "SELECT USER_ID FROM USER_INFO WHERE USER_NAME = '" + request[i] + "'";
						ResultSet resultSet2 = statement2.executeQuery(sql3);
						
						while(resultSet2.next()) {
							userId = resultSet2.getInt(1);
							updateUserGroupTable(groupId.get(0), userId);
						}
					}
					serverOut.flush();
					System.out.println("created entree in group_info table AND user_group table");
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
		
		/**
		 * The connectDatabase method connects to the database server and returns a Connection object. 
		 * 
		 * @return a Connection object
		 */
		public Connection connectDatabase() throws Exception  {
			String url;
			String username;
			String password;

			try (FileInputStream input = new FileInputStream(new File("db.properties"))) {
				Properties props = new Properties();

				props.load(input);

				// String driver = (String) props.getProperty("driver");
				username = (String) props.getProperty("username");
				password = (String) props.getProperty("password");
				url = (String) props.getProperty("URL");

				// We do not need to load the driver explicitly
				// DriverManager takes cares of that
				// Class.forName(driver);		
				
			}
			return DriverManager.getConnection(url, username, password);
		}
	}
}
