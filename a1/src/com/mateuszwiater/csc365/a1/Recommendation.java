package com.mateuszwiater.csc365.a1;

import java.net.URL;

/**
 * Created by Mateusz on 2/11/2015.
 */
public class Recommendation {
    private String title;

    private int similarity;

    private URL url;

    public Recommendation(String title, int similarity, URL url) {
        this.title = title;
        this.similarity = similarity;
        this.url = url;
    }

    public URL getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public int getSimilarity() {
        return similarity;
    }
}
