package org.Googol;

public class Relevance {
    private URL url;
    private int rel;

    public Relevance(URL url, int rel) {
        this.rel = rel;
        this.url = url;
    }

    public String getURL() {
        return url.toString();
    }

    public int getRelevance() {
        return rel;
    }

    public String toString() {
        return "URL: " + url.toString() + "\nRelevance: " + rel + "\n";
    }
}
