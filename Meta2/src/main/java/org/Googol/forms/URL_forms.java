package org.Googol.forms;

import java.io.Serializable;

public class URL_forms implements Serializable{

    private String search_url;
    private int page;
    private int num_results;

    public URL_forms() {
        this.search_url = "";
        this.page = 0;
        this.num_results = 0;
    }

    public URL_forms(String search_url) {
        this.search_url = search_url;
        this.page = 0;
        this.num_results = 0;
    }

    public URL_forms(String search_url,int page) {
        this.search_url = search_url;
        this.page = page;
        this.num_results = 0;
    }

    public URL_forms(String search_url,int page,int num_results) {
        this.search_url = search_url;
        this.page = page;
        this.num_results = num_results;
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
     * @param num_results
     */
    public void setNum_results(int num_results) {
        this.num_results = num_results;
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

    /**
     * 
     * @return
     */
    public int getNum_results() {
        return this.num_results;
    }

}
