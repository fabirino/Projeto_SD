package org.Googol.forms;

import java.io.Serializable;

public class Words implements Serializable{

    private String search_words;

    public Words() {
        this.search_words = "";
    }

    public Words(String search_words) {
        this.search_words = search_words;
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
     * @return
     */
    public String getSearch_words() {
        return this.search_words;
    }

}
