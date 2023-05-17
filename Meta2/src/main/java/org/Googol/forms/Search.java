package org.Googol.forms;

import java.io.Serializable;

public class Search implements Serializable{
    private String word;
    private int count;

    public Search() {
        // Construtor padrão sem argumentos
    }

    public Search(String word, int count) {
        this.word = word;
        this.count = count;
    }

    public String getWord() {
        return word;
    }

    public int getCount() {
        return count;
    }
}
