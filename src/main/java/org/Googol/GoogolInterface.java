package org.Googol;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

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
     * @param word list of words specified by the user
     * @throws RemoteException
     * @return the list of URLs that contain the word specified by the user
     */
    // public void pagesWithWord(String[] word) throws RemoteException;
    public String pagesWithWord(String[] word, int pages) throws RemoteException;

    /**
     * <h4> Funcionalidades 5
     * @param URL URL specified by the user
     * @throws RemoteException
     * @return the list of URLs that contain the URL specified by the user
     */
    public Vector<String> pagesWithURL(String URL) throws RemoteException;

    /**
     * <h4> Funcionalidades 6
     * <p> Show the running config:
     * <li> - List of Downloaders
     * <li> - List of Barrels (IP and Port)
     * <li> - 10 most common searches
     * @throws RemoteException
     */
    public String adminPage() throws RemoteException;

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
     * Used when the System Starts to get the Queue from the last Session or after a System Crash
     * 
     * @throws RemoteException
     */
    public void queueRecovery() throws RemoteException;

    /**
     * Used to save the state of the Queue on a Crash
     * @throws RemoteException
     */
    public void queueCrash() throws RemoteException;
}
