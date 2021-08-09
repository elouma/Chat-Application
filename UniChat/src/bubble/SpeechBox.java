package bubble;


import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.control.Label;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;

import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;


public class SpeechBox extends HBox{
    private Color DEFAULT_SENDER_COLOR = Color.ROYALBLUE;
    private Color DEFAULT_RECEIVER_COLOR = Color.WHITE;
    private Background DEFAULT_SENDER_BACKGROUND, DEFAULT_RECEIVER_BACKGROUND;

    private String user;
    private String message;
    private SpeechDirection direction;

    private TextFlow displayedText;
    private Label label;
    private SVGPath directionIndicator;

    public SpeechBox(String message, String user, SpeechDirection direction){
    	this.user = user;
        this.message = message;
        this.direction = direction;
        initialiseDefaults();
        setupElements();
    }

    private void initialiseDefaults(){
        DEFAULT_SENDER_BACKGROUND = new Background(new BackgroundFill(DEFAULT_SENDER_COLOR, new CornerRadii(5,0,5,5,false), Insets.EMPTY));
        DEFAULT_RECEIVER_BACKGROUND = new Background(new BackgroundFill(DEFAULT_RECEIVER_COLOR, new CornerRadii(0,5,5,5,false), Insets.EMPTY));
    }

    private void setupElements(){
    	SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss a"); 
        Date date = new Date();
        String time = sdf.format(date);
        Text messageTime = new Text(time);
    	Text messageBody = new Text(message + "\n");
    	Text messageUser = new Text(user + "\n");
    	messageTime.setFont(Font.font("Arial", 8));
    	messageBody.setFont(Font.font("Arial", 15));
    	messageUser.setFont(Font.font("Arial",FontWeight.BOLD,10));
    	messageTime.setFill(Color.GRAY);
    	messageBody.setFill(Color.BLACK);
    	messageUser.setFill(Color.ROYALBLUE);
    	       
        if(direction == SpeechDirection.LEFT){
        	TextFlow displayedText = new TextFlow(messageUser, messageBody, messageTime);
        	displayedText.setPadding(new Insets(5));
        	directionIndicator = new SVGPath();	
            Label label = new Label(null,displayedText);
            
        	displayedText.setBackground(DEFAULT_RECEIVER_BACKGROUND);
            displayedText.setTextAlignment(TextAlignment.LEFT);
            directionIndicator.setContent("M0 0 L10 0 L10 10 Z");
            directionIndicator.setFill(DEFAULT_RECEIVER_COLOR);
          
            HBox container = new HBox(directionIndicator, displayedText);
            //Use at most 60% of the width provided to the SpeechBox for displaying the message
            container.maxWidthProperty().bind(widthProperty().multiply(0.6));
            getChildren().setAll(container);
            setAlignment(Pos.CENTER_LEFT);
            
        }
        else{
            TextFlow displayedText = new TextFlow(messageBody, messageTime);
        	displayedText.setPadding(new Insets(5));
        	directionIndicator = new SVGPath();	
            Label label = new Label(null,displayedText);
            
        	displayedText.setBackground(DEFAULT_SENDER_BACKGROUND);
            displayedText.setTextAlignment(TextAlignment.LEFT);
            directionIndicator.setContent("M10 0 L0 10 L0 0 Z");
            directionIndicator.setFill(DEFAULT_SENDER_COLOR);
           
            HBox container = new HBox(displayedText, directionIndicator);
            //Use at most 60% of the width provided to the SpeechBox for displaying the message
            container.maxWidthProperty().bind(widthProperty().multiply(0.6));
            getChildren().setAll(container);
            setAlignment(Pos.CENTER_RIGHT);

        }
    }
}