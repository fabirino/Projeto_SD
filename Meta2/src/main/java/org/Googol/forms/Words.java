package org.Googol.forms;

import java.io.Serializable;

public class Words implements Serializable{

    private String search_words;
    private int page;

    public Words() {
        this.search_words = "";
        this.page = 0;
    }

    public Words(String search_words) {
        this.search_words = search_words;
        this.page = 0;
    }

    public Words(String search_words,int page) {
        this.search_words = search_words;
        this.page = page;
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

}
