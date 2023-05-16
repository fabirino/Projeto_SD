package org.Googol;

import java.io.Serializable;

public class Stats implements Serializable {
    private String[] searches;

    public Stats() {
        // Construtor padrão sem argumentos
    }

    public Stats(String[] searches) {
        this.searches = searches;
    } 

    public String[] getSearches() {
        return searches;
    }

    public void setSearches(String[] searches) {
        this.searches = searches;
    }
}
