package org.Googol;

import java.io.Serializable;

public class Message implements Serializable{

    private URL url;
    private boolean zip;
    private int lenght;
    private int PORT;

    
    // Construtors ==============================================================================
    public Message(URL url,boolean zip,int lenght,int PORT){
        this.url = url;
        this.zip = zip;
        this.lenght = lenght;
        this.PORT = PORT;
    }

    public Message(URL url,boolean zip,int PORT){
        this.url = url;
        this.zip = zip;
        this.lenght = 0;
        this.PORT = PORT;
    }

    public Message(boolean zip,int lenght,int PORT){
        this.url = null;
        this.zip = zip;
        this.lenght = lenght;
        this.PORT = PORT;
    }

    // Getters ==================================================================================
    public URL getURL(){
        return url;
    }

    public boolean getZip(){
        return zip;
    }

    public int getLenght(){
        return lenght;
    }

    public int getPORT(){
        return PORT;
    }

    public String toString(){
        return "URL: " + url + "\nZip: " + zip + "\nLenght: " + lenght + "\n" + "\nPORT: " + PORT + "\n";
    }

}
