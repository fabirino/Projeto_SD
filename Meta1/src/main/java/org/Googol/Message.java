package org.Googol;

import java.io.Serializable;

/**
 * Class usada para armazenar a informacao enviada por Multicast dos Downloaders para os Barrels
 */

public class Message implements Serializable{

    private URL url;
    private int PORT;
    private String IP;

    
    // Construtors ==============================================================================
    public Message(URL url, int PORT){
        this.url = url;
        this.PORT = PORT;
        this.IP = null;
    }
    public Message(URL url, int PORT,String ip){
        this.url = url;
        this.PORT = PORT;
        this.IP = ip;
    }

    // Getters ==================================================================================
    /**
     * Get Method
     * @return the URL in the message
     */
    public URL getURL(){
        return url;
    }


    /**
     * Get Method
     * @return UDP PORT used to communicate between a specific Barrel and the Downloader
     */
    public int getPORT(){
        return PORT;
    }

    /**
     * Get Method
     * @return toString
     */
    public String toString(){
        return "URL: " + url + "\n" + "\nPORT: " + PORT + "\n";
    }

    /**
     * Get Method
     * @return UDP IP used to communicate between a specific Barrel and the Downloader
     */
    public String getIP() {
        return IP;
    }

}
