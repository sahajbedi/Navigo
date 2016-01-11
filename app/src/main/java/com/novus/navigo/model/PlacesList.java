package com.novus.navigo.model;

import java.util.List;

/**
 * Created by sahajbedi on 11-Jan-16.
 */
public class PlacesList {
    String next_page_token;
    List<Place> results;
    String status;

    public List<Place> getResults() {
        return results;
    }

    public void setResults(List<Place> results) {
        this.results = results;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNext_page_token() {
        return next_page_token;
    }

    public void setNext_page_token(String next_page_token) {
        this.next_page_token = next_page_token;
    }
}
