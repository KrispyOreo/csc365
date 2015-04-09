package com.mateuszwiater.csc365.a2;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by Mateusz on 3/24/2015.
 */
public class Application extends javafx.application.Application{

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Save the application instance
        Loader.getInstance().setApplication(this);
        // Load the FXML file
        Parent root = FXMLLoader.load(getClass().getResource("MainWindow.fxml"));
        // Set the window title
        primaryStage.setTitle("Assignment 2");
        // Set the window style to the FXML file
        primaryStage.setScene(new Scene(root));
        // Disable re-sizing
        primaryStage.setResizable(false);
        // Show the window
        primaryStage.show();
    }


    public static void main(String[] args) throws IOException {
        // Start the application
        launch(args);
    }
}
