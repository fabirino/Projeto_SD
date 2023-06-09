package org.Googol.forms;

import java.io.Serializable;

public class Words implements Serializable{

    private String search_words;
    private int page;
    private int num_results;

    public Words() {
        this.search_words = "";
        this.page = 0;
        this.num_results = 0;
    }

    public Words(String search_words) {
        this.search_words = search_words;
        this.page = 0;
        this.num_results = 0;
    }

    public Words(String search_words,int page) {
        this.search_words = search_words;
        this.page = page;
        this.num_results = 0;
    }

    public Words(String search_words,int page,int num_results) {
        this.search_words = search_words;
        this.page = page;
        this.num_results = num_results;
    }

    /**
     * 
     * @param search_words
     */
    public void setSearch_words(String search_words) {
        this.search_words = search_words;
    }

    /**
     * 
     * @param page
     */
    public void setPage(int page) {
        this.page = page;   
    }

    /**
     * 
     * @param num_results
     */
    public void setNum_results(int num_results) {
        this.num_results = num_results;
    }


    /**
     * 
     * @return
     */
    public String getSearch_words() {
        return this.search_words;
    }

    /**
     * 
     * @return
     */
    public int getPage() {
        return this.page;
    }

    /**
     * 
     * @return
     */
    public int getNum_results() {
        return this.num_results;
    }

}
