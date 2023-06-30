package View;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.google.gson.Gson;

import Controller.Orders;
import Main.Client;
import Main.NormalRequest;
import Main.Request;
import Model.User;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class UserSearchBar {
    private VBox mainVbox;
    private UserInfoHbox targetUserInfoHbox;
    private TextField searchingField;
    private Button searchButton;
    private Text outputText;
    

    public UserSearchBar(){
        this.mainVbox= new VBox(8);
        initalizeSearchField();
    }

    private void initalizeSearchField(){
        this.searchingField= new TextField();
        searchingField.setPromptText("Friend Username to search");

        Image searchImage=null;
        try {
            searchImage=new Image(new FileInputStream("src/main/resources/Images/Icon/searchIcon.png"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ImageView searchImageView=new ImageView(searchImage); searchImageView.setFitHeight(48);searchImageView.setFitWidth(48);
        HBox temp1=new HBox(0, searchImageView,searchingField);
        temp1.setMaxWidth(120);temp1.setMinWidth(120);
        initializeSerachButton();

        HBox temp2=new HBox(8, temp1,searchButton);
        outputText=new Text();
        outputText.setVisible(false);

        mainVbox.getChildren().addAll(temp2,outputText);
    }

    private void initializeSerachButton(){
        searchButton=new Button("Search");
        searchButton.setMinWidth(64);
        searchButton.setMaxWidth(64);
        searchButton.setMinHeight(48);
        searchButton.setMaxHeight(48);
        searchButton.setOnMouseClicked(event -> searchForUser());
    }

    private void searchForUser(){

        mainVbox.getChildren().remove(targetUserInfoHbox.getMainHbox());

        String targetUsername=searchingField.getText();
        Request request=new Request(NormalRequest.GET_USER_BY_USERNAME);
        request.addToArguments("Username", targetUsername);
        Client.client.sendRequestToServer(request, true);

        User targetUser=new Gson().fromJson(Client.client.getRecentResponse(), User.class);
        if(targetUser==null) 
            Orders.sendTextNotification(outputText, "User Not Found", Orders.redNotifErrorColor, mainVbox);
    
        else{
            targetUserInfoHbox= new UserInfoHbox(targetUser);
            mainVbox.getChildren().add(targetUserInfoHbox.getMainHbox());
        }
    }

}
