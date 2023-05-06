package org.Googol.forms;

import java.io.Serializable;

public class URL_forms implements Serializable{

    private String search_url;

    public URL_forms() {
        this.search_url = "";
    }

    public URL_forms(String search_url) {
        this.search_url = search_url;
    }

    /**
     * 
     * @param search_url
     */
    public void setSearch_url(String search_url) {
        this.search_url = search_url;
    }

    /**
     * 
     * @return
     */
    public String getSearch_url() {
        return this.search_url;
    }

}
