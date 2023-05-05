package org.Googol.forms;

import java.io.Serializable;

/**
 * User
 */
public class User implements Serializable{

    private String name;
    private String password;

    public User() {
        this.name = "";
        this.password = "";
    }

    /**
     * 
     * @param name
     * @param password
     */
    public User(String name,String password) {
        this.name = name;
        this.password = password;
    }

    /**
     * 
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * 
     * @return
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * 
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @param Password
     */
    public void setPassword(String Password) {
        this.password = Password;
    }



}
