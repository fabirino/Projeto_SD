package org.Googol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class URL implements Serializable{
    private String url;
    private String title;
    private String quote;
    private List<String> keywords;
    private List<String> urls;

    // Construtors ==============================================================================
    public URL(String url, String title,String quote, List<String> keywords, List<String> urls){
        this.url = url;
        this.title = title;
        this.quote = quote;
        this.keywords = keywords;
        this.urls = urls;
    }

    public URL(String url){
        this.url = url;
        this.title = "";
        this.quote = "";
        this.keywords = new ArrayList<String>();
        this.urls = new ArrayList<String>();
    }

    // Setters ==================================================================================
    public void setTitle(String title){
        this.title = title;
    }

    public void setQuote(String quote){
        this.quote = quote;
    }

    public boolean addKeyword(String word){
        return keywords.add(word);
    }


    public boolean addURL(String url){
        return urls.add(url);
    }


    // Getters ==================================================================================
    public String getUrl(){
        return url;
    }

    public List<String> getUrls(){
        return urls;
    }

    public List<String> getKeywords(){
        return keywords;
    }

    public String toString(){
        return "URL: " + url + "\nTitle: " + title + "\nQuote: " + quote;
    }

}