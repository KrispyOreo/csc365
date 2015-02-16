package com.mateuszwiater.csc365.a1;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.MalformedURLException;
import java.net.URL;

public class MainController {

    public TextField urlField;

    String urlError = "ERROR! Bad URL, Try Again.";

    public void showRecommendation(ActionEvent actionEvent) throws Exception {
        // Generate the recommendation
        try {
            Data.getInstance().generateRecommendation(new URL(urlField.getText()));
        } catch (MalformedURLException e) {
            // Bad URL, write an error message
            urlField.setText(urlError);
            // Set the error message to the color red
            urlField.setStyle("-fx-text-inner-color: red;");
            return;
        }
        // Load the FXML file
        Parent root = FXMLLoader.load(getClass().getResource("RecommendationWindow.fxml"));
        // Create a new window
        Stage stage = new Stage();
        // Set the title of the window
        stage.setTitle("Success");
        // Set the window style to the FXML file
        stage.setScene(new Scene(root));
        // Disable re-sizing
        stage.setResizable(false);
        // Show the window
        stage.show();
    }

    public void resetUrlField(Event event) {
        // Check if the error message is shown
        if(urlField.getText().equalsIgnoreCase(urlError)) {
            // Clear the text field
            urlField.setText("");
            // Set the text color to red
            urlField.setStyle("-fx-text-inner-color: black;");
        }
    }
}
