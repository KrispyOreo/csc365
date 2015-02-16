package com.mateuszwiater.csc365.a1;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Mateusz on 2/9/2015.
 */
public class RecommendationController implements Initializable {

    public Label websiteTitle;
    public Label similarityPercentage;

    public Hyperlink url;

    @Override // Ran when window is opened to set up values
    public void initialize(URL location, ResourceBundle resources) {
        // Get a Recommendation instance
        Recommendation recommendation = Data.getInstance().getRecommendation();
        // Set the website title
        websiteTitle.setText(recommendation.getTitle());
        // Set the website similarity percentage
        similarityPercentage.setText(Integer.toString(recommendation.getSimilarity()) + "%");
        // Set the website URL
        url = new Hyperlink(recommendation.getUrl().toString());
    }

    public void goToWebPage(ActionEvent actionEvent) {
        // Go to the website
        Data.getInstance().getApplication().getHostServices().showDocument(url.getText());
        // Close the window
        ((Node)(actionEvent.getSource())).getScene().getWindow().hide();
    }

}

