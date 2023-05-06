package org.Googol.forms;

import java.io.Serializable;

public class URL_forms implements Serializable{

    private String search_url;
    private int page;

    public URL_forms() {
        this.search_url = "";
        this.page = 1;
    }

    public URL_forms(String search_url) {
        this.search_url = search_url;
        this.page = 1;
    }

    public URL_forms(String search_url,int page) {
        this.search_url = search_url;
        this.page = page;
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
     * @param page
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * 
     * @return
     */
    public String getSearch_url() {
        return this.search_url;
    }

    /**
     * 
     * @return
     */
    public int getPage() {
        return this.page;
    }

}
