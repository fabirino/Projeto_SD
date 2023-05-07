package org.Googol;

import java.io.Serializable;

public class Response implements Serializable{
    
    private String text;
    private int length;
    
    public Response(){
        text = "";
        length = 0;
    }

    /**
     * Constructor
     * @param text
     * @param length
     */
    public Response(String text, int length){
        this.text = text;
        this.length = length;
    }

    // Setters ==================================================================================
    public void setText(String text){
        this.text = text;
    }

    public void setLength(int length){
        this.length = length;
    }

    // Getters ==================================================================================
    public String getText(){
        return this.text;
    }

    public int getLength(){
        return this.length;
    }

}
