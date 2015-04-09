package com.mateuszwiater.csc365.a2;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Mateusz on 2/9/2015.
 */
public class RecommendationController implements Initializable {

    public Hyperlink rec1;
    public Hyperlink rec2;
    public Hyperlink rec3;

    public Label timeIndicator;

    @Override // Ran when window is opened to set up values
    public void initialize(URL location, ResourceBundle resources) {
        // Get a Recommendation instance
        Loader.Recommendation[] recommendations = new Loader.Recommendation[0];
        try {
            recommendations = Loader.getInstance().getRecommendations();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Set the recommended website titles
        String title = recommendations[0].getPage().getTitle();
        title = (title.length() > 30) ? title.substring(0,30) + "..." : title;
        rec1.setText(title);
        title = recommendations[1].getPage().getTitle();
        title = (title.length() > 30) ? title.substring(0,30) + "..." : title;
        rec2.setText(title);
        title = recommendations[2].getPage().getTitle();
        title = (title.length() > 30) ? title.substring(0,30) + "..." : title;
        rec3.setText(title);
        // Set the recommended website URLs
        rec1 = new Hyperlink(recommendations[0].getPage().getUrl().toString());
        rec2 = new Hyperlink(recommendations[1].getPage().getUrl().toString());
        rec3 = new Hyperlink(recommendations[2].getPage().getUrl().toString());
        // Set the timeIndicator
        try {
            timeIndicator.setText(Loader.getInstance().getLoadTime());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goToRec1(ActionEvent actionEvent) throws IOException {
        // Go to the website
        Loader.getInstance().getApplication().getHostServices().showDocument(rec1.getText());
    }

    public void goToRec2(ActionEvent actionEvent) throws IOException {
        // Go to the website
        Loader.getInstance().getApplication().getHostServices().showDocument(rec2.getText());
    }

    public void goToRec3(ActionEvent actionEvent) throws IOException {
        // Go to the website
        Loader.getInstance().getApplication().getHostServices().showDocument(rec3.getText());
    }

}

