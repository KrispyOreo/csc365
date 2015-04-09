package com.mateuszwiater.csc365.a2;

import javafx.application.Application;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Mateusz on 3/24/2015.
 */
public class Loader {

    String cacheName = "Cache.dat";
    String directory = "data";
    String loadTime;

    Application application;

    MainController mainController;

    Cache cache;

    private Recommendation[] recommendations;

    private static Loader instance;

    // Return the instance of the Data class
    public static Loader getInstance() throws IOException {
        // Check if an instance already exists
        if(instance == null) {
            // Create a new instance if it does not
            instance = new Loader();
            // Return the instance
            return instance;
        } else {
            // return the instance if it does
            return instance;
        }
    }

    private Loader() throws IOException {
        // Check if the directory exists
        File tmpFile = new File(directory);
        if(!tmpFile.exists()) {
            tmpFile.mkdir();
        }
        // Check if the cache exists
        if(new File(cacheName).exists()) {
            System.out.println("Starting Update...");
            // Cache exists, load it and check for updates
            cache = new Cache(cacheName);
            // Check each loaded website if it needs updating
            int counter = 0;
            for(String url : cache.getUrls()) {
                counter++;
                System.out.println("\tChecking page " + counter + " of " + cache.getSize());
                PageHandler pageHandler = new PageHandler(new URL(url));
                if(cache.getLastModified() < pageHandler.getLastModified()) {
                    System.out.println("\tUpdating page...");
                    // Page needs to update
                    // Remove the page bTree
                    Path bTreePath = FileSystems.getDefault().getPath(directory, cache.get(url));
                    Files.delete(bTreePath);
                    // Create a new bTree
                    BTree bTree = new BTree(bTreePath.toString(), url);
                    // Populate the bTree
                    HashMap<String, Integer> words = pageHandler.getWords();
                    for(String word : words.keySet()) {
                        bTree.add(word, words.get(word));
                    }
                    //Close the bTree
                    bTree.close();
                }
            }
            System.out.println("Done Updating!");
            // Set the cache
            cache.save();
        } else {
            System.out.println("Starting Initial Load...");
            // Cache does not exist, create it
            cache = new Cache(cacheName);
            // Loop through the root pages
            int counter1 = 1;
            int counter2 = 1;
            for(URL url : getRootPages()) {
                System.out.println("Loading Root Page: " + counter1);
                counter1++;
                String bTreeName = UUID.randomUUID().toString() + ".dat";
                // Add to cache
                cache.put(url.toString(), bTreeName);
                // Populate bTree
                BTree bTree = new BTree(FileSystems.getDefault().getPath(directory, bTreeName).toString(), url.toString());
                PageHandler rootHandler = new PageHandler(url);
                HashMap<String, Integer> words = rootHandler.getWords();
                for(String word : words.keySet()) {
                    bTree.add(word, words.get(word));
                }
                // Close the bTree
                bTree.close();
                // Loop through the sub pages
                for(URL subUrl : rootHandler.getLinks()) {
                    System.out.println("\tLoading Sub Page: " + counter2);
                    counter2++;
                    bTreeName = UUID.randomUUID().toString() + ".dat";
                    // Add to cache
                    cache.put(subUrl.toString(), bTreeName);
                    // Populate the bTree
                    bTree = new BTree(FileSystems.getDefault().getPath(directory, bTreeName).toString(), subUrl.toString());
                    PageHandler subHandler = new PageHandler(subUrl);
                    words = subHandler.getWords();
                    for(String word : words.keySet()) {
                        bTree.add(word, words.get(word));
                    }
                    // Close the bTree
                    bTree.close();
                }
                counter2 = 1;
            }
            System.out.println("Done Loading!");
            // Save the cache
            cache.save();
        }
    }


    public void getRecommendations(URL url, MainController mainController) throws IOException {
        // Set the start time
        long startTime = System.currentTimeMillis();
        // Set the main controller
        this.mainController = mainController;
        // PageHandler to take care of getting the words
        PageHandler ph = new PageHandler(url);
        // List of words from the site
        HashMap<String, Integer> words = ph.getWords();
        // Store the top 3 recommendations
        Recommendation[] recommendations = new Recommendation[3];
        // Hold the recommended similarity
        int similarity = 0;
        // Create the similarity
        for(String key : words.keySet()) {
            similarity += words.get(key);
        }
        // Temporary similarity percentage
        int tmpSim = 0;
        // Cycle through each website
        for(String websiteUrl : cache.getUrls()) {
            // Get the websites words and build the tmpSim
            BTree bTree = new BTree(FileSystems.getDefault().getPath(directory, cache.get(websiteUrl)).toString(), websiteUrl);
            HashMap<String, Integer> compareToWords = bTree.getBulkFrequencys(words);
            for(String word : compareToWords.keySet()) {
                tmpSim += compareToWords.get(word);
            }

            int offset = Math.abs(similarity - tmpSim);
            // Set the recommendation
            for(int i = 0; i < 3; i++) {
                // Check if a recommendation exists
                if(recommendations[i] != null) {
                    if(recommendations[i].getOffset() > offset) {
                        // Push the recommendations
                        Recommendation tmpRec = recommendations[i];
                        Recommendation tmpRec2;
                        for(int j = i+1; j < 3; j++) {
                            tmpRec2 = recommendations[j];
                            recommendations[j] = tmpRec;
                            tmpRec = tmpRec2;
                        }
                        // Set the new recommendation
                        recommendations[i] = new Recommendation(websiteUrl, offset);
                        break;
                    }
                } else {
                    // Set the new recommendation
                    recommendations[i] = new Recommendation(websiteUrl, offset);
                }
            }
            tmpSim = 0;
        }
        // Calculate how long it took to recommend the website
        long endTime        = System.currentTimeMillis();
        long totalTime      = endTime - startTime;
        long minutes        = TimeUnit.MILLISECONDS.toMinutes(totalTime);
        long seconds        = TimeUnit.MILLISECONDS.toSeconds(totalTime) - TimeUnit.MINUTES.toSeconds(minutes);
        long milliseconds   = totalTime - (TimeUnit.SECONDS.toMillis(seconds) + TimeUnit.MINUTES.toMillis(minutes));
        loadTime = minutes + " Minutes " + seconds + " Seconds " + milliseconds + " Milliseconds";
        this.recommendations = recommendations;
    }

    private ArrayList<URL> getRootPages() {
        // Read the file
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(new File(getClass().getResource("websites.txt").getPath())));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // URL of a website
        URL url;
        // ArrayList for the URL's
        ArrayList<URL> tmp = new ArrayList<URL>();

        // Loop over the lines
        try {
            while (in.ready()) {
                // Store the URL
                url = new URL(in.readLine());
                // Store the html file of the website
                tmp.add(url);
            }
            // Close the buffered reader
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public class PageHandler {
        URL url;

        Document doc = null;

        public PageHandler(URL url) {
           this.url = url;
        }

        public HashMap<String,Integer> getWords() {
            loadPage();
            // Create a regex pattern
            Pattern regex = Pattern.compile("([A-Za-z])+");

            // Strip the HTML from the file
            String stripped = doc.text();

            // Create a regex matcher
            Matcher regexMatcher = regex.matcher(stripped);

            // Create the words ArrayList
            HashMap<String, Integer> words = new HashMap<String, Integer>();

            // Loop though all of the matched words and separate them by commas
            while(regexMatcher.find()) {
                // Get the starting position of the text
                int start = regexMatcher.start(0);
                // Get ending position of the text
                int end = regexMatcher.end(0);
                // Get the word
                String word = stripped.substring(start, end).toLowerCase();
                // Add the word to the arrayList
                if(words.containsKey(word)) {
                    // Increment the word value in the HashMap
                    words.put(word, words.get(word) + 1);
                } else {
                    // Put the word into the HashMap
                    words.put(word, 1);
                }
            }

            // Return the words HashMap
            return words;
        }


        public ArrayList<URL> getLinks() {
            int linkLimit = 10;

            int counter = 0;

            loadPage();
            ArrayList<URL> tmp = new ArrayList<URL>();

            URL url;

            for(Element link : doc.select("a[href]")) {
                try {
                    url = new URL(link.attr("abs:href"));
                    Connection.Response resp = Jsoup.connect(url.toString()).timeout(10 * 1000).followRedirects(true).execute();
                    String contentType = resp.contentType();

                    Pattern regex = Pattern.compile("^text\\/|^application\\/xml|^application\\/xhtml\\+xml");

                    Matcher regexMatcher = regex.matcher(contentType);

                    while (regexMatcher.find()) {
                        boolean addLink = true;
                        // Check the cache if this link exists
                        for(String tmpUrl : cache.getUrls()) {
                            URL cachedUrl = new URL(tmpUrl);
                            if(url.sameFile(cachedUrl)) {
                                // Same url found, add a different one
                                addLink = false;
                                break;
                            }
                        }
                        // Check the current ArrayList if the link exists
                        for(URL tmpUrl : tmp) {
                            if(url.sameFile(tmpUrl)) {
                                // Same url found, add a different one
                                addLink = false;
                                break;
                            }
                        }
                        // Add the url
                        if(addLink) {
                            tmp.add(url);
                            counter++;
                            break;
                        }
                    }
                    if (counter >= linkLimit) {
                        return tmp;
                    }
                } catch (HttpStatusException e) {

                } catch (UnsupportedMimeTypeException e) {

                } catch (MalformedURLException e) {

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return tmp;
        }

        public long getLastModified() throws IOException {
            URLConnection connection = url.openConnection();
            return connection.getLastModified();
        }

        public String getTitle() {
            loadPage();
            return doc.title();
        }

        private void loadPage() {
            try {
                if(doc == null) {
                    doc = Jsoup.connect(url.toString()).followRedirects(true).userAgent("Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0").get();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public URL getUrl() {
            return url;
        }
    }

    public class Recommendation {
        int offset = 0;

        PageHandler page;

        public Recommendation(String url, int offset) throws MalformedURLException {
            this.offset = offset;
            this.page = new PageHandler(new URL(url));
        }

        public int getOffset() {
            return offset;
        }

        public PageHandler getPage() {
            return page;
        }
    }

    public String getLoadTime() {
        return loadTime;
    }

    public Recommendation[] getRecommendations() {
        return recommendations;
    }

    // Get the Application reference
    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }
}