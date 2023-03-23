package org.Googol;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GoogolInterface extends Remote{
    /**
     * <h4> Funcionalidades 1 e 2
     * <p> Add the URL to the QUEUE and if this URL has links, it adds them to the QUEUE as well
     *
     * @param URL URL specified by the user
     * @throws RemoteException
     */
    public void newURL(String URL) throws RemoteException;

    /**
     * <h4> Funcionalidades 3 e 4
     * <p> TODO: substituir void por uma lista threadsafe
     * @param word list of words specified by the user
     * @throws RemoteException
     * @return the list of URLs that contain the word specified by the user
     */
    // public void pagesWithWord(String[] word) throws RemoteException;
    public String pagesWithWord(String word) throws RemoteException;

    /**
     * <h4> Funcionalidades 5
     * <p> TODO: substituir void por uma lista threadsafe
     * @param URL URL specified by the user
     * @throws RemoteException
     * @return the list of URLs that contain the URL specified by the user
     */
    public void pagesWithURL(String URL) throws RemoteException;

    /**
     * <h4> Funcionalidades 6
     * <p> Show the running config:
     * <li> List of Downloaders
     * <li> List of Barrels (IP and Port)
     * <li> 10 most common searches
     * @throws RemoteException
     */
    public void adminPage() throws RemoteException;

    /**
     *
     * @return Return the String containing all the avalable commands
     * @throws RemoteException
     */
    public String menu() throws RemoteException;


    // #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=
    // QUEUE functions
    // #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=
    /**
     *
     * @param url to add
     * @return sucess
     * @throws RemoteException
     */
    public boolean addURLQueue(URL URL) throws RemoteException;


    /**
     *
     * @return next URL to index
     * @throws RemoteException
     */
    public URL getURLQueue() throws RemoteException, InterruptedException;
}
