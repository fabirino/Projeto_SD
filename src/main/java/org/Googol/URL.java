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
    /**
     * Constructor
     * @param url
     * @param title
     * @param quote
     * @param keywords
     * @param urls
     */
    public URL(String url, String title,String quote, List<String> keywords, List<String> urls){
        this.url = url;
        this.title = title;
        this.quote = quote;
        this.keywords = keywords;
        this.urls = urls;
    }

    /**
     * Conostrutor
     * @param url
     */
    public URL(String url){
        this.url = url;
        this.title = "";
        this.quote = "";
        this.keywords = new ArrayList<String>();
        this.urls = new ArrayList<String>();
    }

    // Setters ==================================================================================
    /**
     * Set Method
     * @param title
     */
    public void setTitle(String title){
        this.title = title;
    }

    /**
     * Set Method
     * @param quote
     */
    public void setQuote(String quote){
        this.quote = quote;
    }

    /**
     * Adds to the list of keywords
     * @param word
     * @return
     */
    public boolean addKeyword(String word){
        return keywords.add(word);
    }

    /**
     * Adds to the list of URLs
     * @param url
     * @return
     */
    public boolean addURL(String url){
        return urls.add(url);
    }


    // Getters ==================================================================================
    /**
     * Get Method
     * @return URL
     */
    public String getUrl(){
        return url;
    }

    /**
     * Get Method
     * @return list of URLs
     */
    public List<String> getUrls(){
        return urls;
    }

    /**
     * Get Method
     * @return list of keywords
     */
    public List<String> getKeywords(){
        return keywords;
    }

    /**
     * Set Method
     * @return url
     */
    public String toString(){
        return  url;
    }

    /**
     * toString
     * @return String
     */
    public String printURL(){
        return "URL: " + url + "\nTitle: " + title + "\nQuote: " + quote + "\n";
    }

}