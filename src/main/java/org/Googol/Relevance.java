package org.Googol;

/**
 * Classe utilizada para ordenar os resultados das pesquisas por ordem de relevancia
 */
public class Relevance {
    private URL url;
    private int rel;

    /**
     * Constructor
     * @param url URL
     * @param rel relevance value
     */
    public Relevance(URL url, int rel) {
        this.rel = rel;
        this.url = url;
    }

    /**
     * Get method
     * @return URL
     */
    public String getURL() {
        return url.printURL();
    }

    /**
     * Get Method
     * @return Relevance
     */
    public int getRelevance() {
        return rel;
    }

    /**
     * Get Method
     * @return toString
     */
    public String toString() {
        return "URL: " + url.toString() + "\nRelevance: " + rel + "\n";
    }
}
