package com.mateuszwiater.csc365.a1;

import javafx.application.Application;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Mateusz on 2/9/2015.
 */
public class Data {
    private static Data instance;

    private Application application;

    private Recommendation recommendation;

    private ArrayList<WebSite> websites = new ArrayList<WebSite>();

    protected Data() {
        // Exists only to defeat instantiation
    }

    // Return the instance of the Data class
    public static Data getInstance() {
        // Check if an instance already exists
        if(instance == null) {
            // Create a new instance if it does not
            instance = new Data();
            // Return the instance
            return instance;
        } else {
            // return the instance if it does
            return instance;
        }
    }

    public void generateRecommendation(URL url) throws Exception {
        // Get the HTML page
        File html = getHTML(url);
        // Get the entered website words
        String[] words = getWords(html).split(",");
        // Delete the html file
        html.delete();
        // Temporary hash table for the entered website
        HashTable tmpHash = new HashTable();
        // Insert each word into the array
        for(String word : words) {
            tmpHash.put(word, 1);
        }
        // Get the keys
        words = tmpHash.getKeys();

        // Hold the recommended title
        String title = "Website Title";
        // Hold the recommended url
        URL rurl = url;
        // Hold the recommended similarity percentage
        int similarity = 0;
        // Temporary similarity percentage
        int tmpSim = 0;
        // Cycle through each website words table
        for(WebSite website : websites) {
            // Cycle through the entered websites words to generate the similarity percentage
            for(String word : words) {
                tmpSim += ((website.getHash().get(word) / tmpHash.get(word)) * 100);
            }
            // Calculate tmpSim
            tmpSim = (tmpSim / words.length);
            // Set the recommendation
            if(Math.abs(tmpSim - 100) < Math.abs(similarity - 100) || similarity == 0) {
                // Set tmpSim as the current similarity percentage
                similarity = tmpSim;
                // Set the new website title
                title = website.getTitle();
                // Set the new website url
                rurl = website.getUrl();
            }
        }
        //Calculate the substring end point

        if(title.length() > 30) {
            recommendation = new Recommendation(title.substring(0,30) + "...", similarity, rurl);
        } else {
            recommendation = new Recommendation(title, similarity, rurl);
        }
    }

    public void loadReferenceSites(File file) throws Exception{
        // Read the file
        BufferedReader in = new BufferedReader(new FileReader(file));
        // String array to store the words
        String[] words;
        // URL of a website
        URL url;
        // Website HTML file
        File html;
        // HashTable for website
        HashTable table;

        // Loop over the lines
        while (in.ready()) {
            // Store the URL
            url = new URL(in.readLine());
            // Store the html file of the website
            html = getHTML(url);
            // Split the string for processing
            words = getWords(html).split(",");
            // Create the HashTable
            table = new HashTable();
            // Insert each word into the array
            for(String word : words) {
                table.put(word,1);
            }
            // Create the main arrayList entry
            websites.add(new WebSite(Jsoup.parse(html, "UTF-8").title(), url, table));
            // Delete the html file
            html.delete();
        }

        // Close the buffered reader
        in.close();
    }

    private File getHTML(URL url) throws Exception{
        // Check if file exists
        File file = new File("tmp.html");
        if(file.exists()) {
            file.delete();
        }
        // Create a http Connection
        HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
        // "Trick" the website into thinking you are a browser
        httpcon.addRequestProperty("User-Agent", "Mozilla/4.0");
        // Create a readable byte channel on the connection
        ReadableByteChannel rbc = Channels.newChannel(httpcon.getInputStream());
        // Create the file output stream to write the file from the stream
        FileOutputStream fos = new FileOutputStream("tmp.html");
        // Write to the file from the url stream
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

        // Close the readable byte channel
        rbc.close();
        // Close the file output stream
        fos.close();

        // Open the html file
        return new File("tmp.html");
    }

    public String getWords(File html) throws Exception {
        // Create a regex pattern
        Pattern regex = Pattern.compile("([A-Za-z])+");

        // Strip the HTML from the file
        String stripped = Jsoup.parse(html, "UTF-8").text();

        // Create a regex matcher
        Matcher regexMatcher = regex.matcher(stripped);

        // Create the words string
        String words = "";

        // Loop though all of the matched words and separate them by commas
        while(regexMatcher.find()) {
            // Get the starting position of the text
            int start = regexMatcher.start(0);
            // Get ending position of the text
            int end = regexMatcher.end(0);
            // Set words equal to words plus the matched word with a comma before it
            words += "," + stripped.substring(start, end).toLowerCase();
        }

        // Remove the first comma
        words = words.substring(1);

        // Return the words
        return words;
    }

    public Recommendation getRecommendation() {
        return recommendation;
    }

    // Get the Application reference
    public Application getApplication() {
        return application;
    }

    // Set the Application Reference
    public void setApplication(Application application) {
        this.application = application;
    }

    private class WebSite {

        private String title;

        private HashTable hash;

        private URL url;

        public WebSite(String title, URL url, HashTable hash) {
            // Set the tile
            this.title = title;
            // Set the words hash table
            this.hash = hash;
            // Set the URL
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public HashTable getHash() {
            return hash;
        }

        public URL getUrl() {
            return url;
        }
    }
}
