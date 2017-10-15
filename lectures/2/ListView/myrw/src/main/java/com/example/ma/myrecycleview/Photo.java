package com.example.ma.myrecycleview;

import java.io.Serializable;

/**
 * Created by dan.
 */

class Photo implements Serializable {
    private String url;
    private String humanDate;
    private String explanation;

    Photo(String url, String humanDate, String explanation) {
        this.url = url;
        this.humanDate = humanDate;
        this.explanation = explanation;
    }

    String getUrl() {
        return url;
    }

    String getHumanDate() {
        return humanDate;
    }

    String getExplanation() {
        return explanation;
    }
}
