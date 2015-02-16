package com.mateuszwiater.csc365.a1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        // Save the application instance
        Data.getInstance().setApplication(this);
        // Load the reference sites
        Data.getInstance().loadReferenceSites(new File(getClass().getResource("websites.txt").getPath()));
        // Load the FXML file
        Parent root = FXMLLoader.load(getClass().getResource("MainWindow.fxml"));
        // Set the window title
        primaryStage.setTitle("Assignment 1");
        // Set the window style to the FXML file
        primaryStage.setScene(new Scene(root));
        // Disable re-sizing
        primaryStage.setResizable(false);
        // Show the window
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
