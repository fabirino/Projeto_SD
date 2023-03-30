package org.Googol;

import java.io.Serializable;

/**
 * Class usada para armazenar a informacao enviada por Multicast dos Downloaders para os Barrels
 */
public class Message implements Serializable{

    private URL url;
    private boolean zip;
    private int lenght;
    private int PORT;

    
    // Construtors ==============================================================================
    /**
     * Constructor
     * @param url URL
     * @param zip if its zipped
     * @param lenght length of the message
     * @param PORT UDP port used between the comunication
     */
    public Message(URL url,boolean zip,int lenght,int PORT){
        this.url = url;
        this.zip = zip;
        this.lenght = lenght;
        this.PORT = PORT;
    }

    /**
     * Constructor
     * @param url URL
     * @param zip if its zipped
     * @param lenght length of the message
     */
    public Message(URL url,boolean zip,int PORT){
        this.url = url;
        this.zip = zip;
        this.lenght = 0;
        this.PORT = PORT;
    }

    /**
     * Constructor
     * @param zip if its zipped
     * @param lenght length of the message
     * @param PORT UDP port used between the comunication
     */
    public Message(boolean zip,int lenght,int PORT){
        this.url = null;
        this.zip = zip;
        this.lenght = lenght;
        this.PORT = PORT;
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
     * @return if the message is zipped or not
     */
    public boolean getZip(){
        return zip;
    }

    /**
     * Get Method
     * @return the length of the message
     */
    public int getLenght(){
        return lenght;
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
        return "URL: " + url + "\nZip: " + zip + "\nLenght: " + lenght + "\n" + "\nPORT: " + PORT + "\n";
    }

}
