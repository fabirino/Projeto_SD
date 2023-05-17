package org.Googol.forms;

import java.io.Serializable;

public class StatsIP implements Serializable{
    private String word;
    private String count;

    public StatsIP() {
        // Construtor padrão sem argumentos
    }

    public StatsIP(String word, String IP) {
        this.word = word;
        this.count = IP;
    }

    public String getWord() {
        return word;
    }

    public String getCount() {
        return count;
    }
}
