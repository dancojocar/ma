package com.example.ma.myrecycleview;

import java.io.Serializable;

/**
 * Created by dan on 10/11/16.
 */

public class Photo implements Serializable {
    private String url;
    private String humanDate;
    private String explanation;

    public Photo(String url, String humanDate, String explanation) {
        this.url = url;
        this.humanDate = humanDate;
        this.explanation = explanation;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHumanDate() {
        return humanDate;
    }

    public void setHumanDate(String humanDate) {
        this.humanDate = humanDate;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
