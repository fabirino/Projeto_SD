package org.Googol.forms;

import java.io.Serializable;

public class Stats_forms implements Serializable{
    private String[] barrels;
    private String[] downloaders;
    private Search[] topsearches;
    private int countbarrels;
    private int countdownloaders;

    public Stats_forms() {
        // Construtor padrão sem argumentos
    }

    public Stats_forms(String[] barrels, String[] downloaders, Search[] topsearches, int countbarrels, int countdownloaders) {
        this.barrels = barrels;
        this.downloaders = downloaders;
        this.topsearches = topsearches;
        this.countbarrels = countbarrels;
        this.countdownloaders = countdownloaders;
    }

    public String[] getBarrels() {
        return barrels;
    }

    public void setBarrels(String[] barrels) {
        this.barrels = barrels;
    }

    public String[] getDownloaders() {
        return downloaders;
    }

    public void setDownloaders(String[] downloaders) {
        this.downloaders = downloaders;
    }

    public Search[] getTopsearches() {
        return topsearches;
    }

    public void setTopsearches(Search[] topsearches) {
        this.topsearches = topsearches;
    }

    public int getCountbarrels() {
        return countbarrels;
    }

    public void setCountbarrels(int countbarrels) {
        this.countbarrels = countbarrels;
    }

    public int getCountdownloaders() {
        return countdownloaders;
    }

    public void setCountdownloaders(int countdownloaders) {
        this.countdownloaders = countdownloaders;
    }


}
