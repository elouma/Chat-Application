 
package gui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.net.MalformedURLException;


import client.Client;

/**
 * The controller class of Signup page.
 *
 * @author Rumit Mehta
 * @author Hang shi
 * @author Jean Emmanuel Messey-Elouma
 * 
 */
public class SignupController {
	
	static Client client;

    @FXML
    private Label signupStatus;

    @FXML
    private TextField signupFirstname;

    @FXML
    private TextField signupLastname;

    @FXML
    private TextField email;

    @FXML
    private PasswordField psw;

    @FXML
    private PasswordField pswagain;

    @FXML
    private TextField courseType;
    
    @FXML
    private TextField courseName;
    
    @FXML
    private ImageView imageview;


    private File file;



    @FXML
    void uploadImage() {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Select Image File");
    	fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));

    	file = fileChooser.showOpenDialog(null);
    	if(file != null) {
    		try {
                String imageUrl = file.toURI().toURL().toString();

                Image image = new Image(imageUrl);
             
                imageview.setImage(image);
    	    }catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
   
    public void signup() throws IOException {
        boolean flag = false;
        System.out.println(signupLastname);
        System.out.println(signupLastname.getText());
        if (email.getText().length() < 6) {
            signupStatus.setText("Please enter a valid student email");
            email.clear();
        } else if(file == null) {
        	signupStatus.setText("Please upload a picture");
        } else if (psw.getText().equals(pswagain.getText()) && !signupFirstname.getText().isEmpty() &&
        		!signupLastname.getText().isEmpty() &&
        		!email.getText().isEmpty() &&
        		!psw.getText().isEmpty()  &&
        		!courseType.getText().isEmpty() &&
        		!courseName.getText().isEmpty()) {
            flag = client.signup(signupFirstname.getText(),
                    signupLastname.getText(),
                    email.getText().substring(0, 6),
                    psw.getText(),
                    email.getText(),
                    courseType.getText(),
                    courseName.getText(),
                    file);
            if (flag) {
                signupStatus.setText("Sign Up Success");
                // If login successfully, jump into the main page.
                Stage stage = new Stage();
                LoginController.stage = stage;
                Parent root = FXMLLoader.load(getClass().getResource("../Login.fxml"));
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("../Login.css").toExternalForm());
                stage.setTitle("Account Profile Page");
                stage.setScene(scene);
                stage.show();
                // Deletes the login page once logged in
                signupStatus.getScene().getWindow().hide();

            } else {
                signupStatus.setText("Sign Up Failure, please try again");
            }
        } else {
            signupStatus.setText("Please fill all fields and ensure that passwords match");
            psw.clear();
            pswagain.clear();
        }
    }
    
    public static void passClient(Client client) {
        SignupController.client = client;
    }

}