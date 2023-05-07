package org.Googol;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
/**
 * Interface implementada pelo Search Module para comunicar com o Cliente via RMI
 */
public interface GoogolInterface extends Remote{
    /**
     * <h4> Features 1 & 2
     * <p> Add the URL to the QUEUE and if this URL has links, it adds them to the QUEUE as well
     *
     * @param URL URL specified by the user
     * @throws RemoteException
     */
    public void newURL(String URL) throws RemoteException;

    /**
     * <h4> Features 3 & 4
     * <p> Gives the result of a search (set or words) in order by relevance and
     * separated by pages of 10 results each
     * 
     * @param word list of words specified by the user
     * @throws RemoteException
     * @return the list of URLs that contain the word specified by the user
     */
    public Response pagesWithWord(String[] word, int pages) throws RemoteException;

    /**
     * <h4> Feature 5
     * <p> Returns all the URLs that leads to a certain URL given by the Client
     * 
     * @param URL URL specified by the user
     * @throws RemoteException
     * @return the list of URLs that contain the URL specified by the user
     */
    public Response pagesWithURL(String URL, int pages) throws RemoteException;

    /**
     * <h4> Feature 6
     * <p> Show the running config:
     * <li> - List of active Downloaders and its IDs
     * <li> - List of active Barrels and its IDs
     * <li> - 10 most common searches
     * @throws RemoteException
     */
    public String adminPage() throws RemoteException, SQLException;

    /**
     * Shows the menu to the Client
     * @return Return the String containing all the avalable commands
     * @throws RemoteException
     */
    public String menu() throws RemoteException;

    /**
     * Function used to log a user in the System
     * @param username  username of the user
     * @param password  password of the user
     * @return 1 success, 0 wrong password, 2 username not found
     * @throws RemoteException
     */
    public int login(String username, String password) throws RemoteException, SQLException;

    /**
     * Function used to register a user in the System
     * @param username username of the new user
     * @param password password of the new user
     * @return 1 success, 0 username already used
     * @throws RemoteException
     */
    public int register(String username, String password) throws RemoteException, SQLException;

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
