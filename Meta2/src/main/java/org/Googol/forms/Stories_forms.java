package org.Googol.forms;

import java.io.Serializable;

public class Stories_forms implements Serializable{

    private String url;
    private String title;
    private int score;
    private String date;
    private int id;

    public Stories_forms(String url, String title, int score, String date, int id) {
        this.url = url;
        this.title = title;
        this.score = score;
        this.date = date;
        this.id = id;
    }

    public Stories_forms() {
        this.url = "";
        this.title = "";
        this.score = 0;
        this.date = "";
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }
    
    public int getScore() {
        return score;
    }

    public String getDate() {
        return date;
    } 

    public int getId() {
        return id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setScore(int score) {
        this.score = score;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    public String toString() {
        return "URL: " + this.url + "\nTitle: " + this.title + "\nScore: " + this.score + "\nDate: " + this.date;
    }   
    
}
