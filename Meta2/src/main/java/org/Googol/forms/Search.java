package org.Googol.forms;

public class Search {
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
