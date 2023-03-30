package org.Googol;

import java.io.Serializable;

public class Message implements Serializable{

    private URL url;
    private int PORT;

    
    // Construtors ==============================================================================
    public Message(URL url, int PORT){
        this.url = url;
        this.PORT = PORT;
    }

    // Getters ==================================================================================
    public URL getURL(){
        return url;
    }


    public int getPORT(){
        return PORT;
    }

    public String toString(){
        return "URL: " + url + "\n" + "\nPORT: " + PORT + "\n";
    }

}
