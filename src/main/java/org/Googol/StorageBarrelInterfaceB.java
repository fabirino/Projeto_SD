package org.Googol;

import java.rmi.*;
import java.util.HashMap;
import java.util.HashSet;
/**
 * Interface impementada pelos Barrels para responderem às Queries pedidas pelos Clientes
 */
public interface StorageBarrelInterfaceB extends Remote{
    /**
     * <p> Used to return all the URLs that contain a Set of Words given by the Client
     * <p> Results are ordered by relevance and separated by pages of 10 URLs each
     * @param Keywords set of words given by the client
     * @param pages correspondent page of the search
     * @return Formated String containing the information of all URLs of that page
     * @throws RemoteException
     */
    public String getUrlsToClient(String[] Keywords, int pages) throws RemoteException;

    /**
     * <p> Used to return all the URLs taht lead to a certain URL
     * <p> Results separated by pages of 10 URLs each
     * @param URL URL given by the client
     * @param pages correspondent page of the serach
     * @return
     * @throws RemoteException
     */
    public HashSet<URL> getpagesWithURL(String URL, int pages) throws RemoteException;

    /**
     * Get Method
     * @return id of the Barrel
     * @throws RemoteException
     */
    public int getId() throws RemoteException;

    /**
     * Used to verify if a Barrel is still active
     * @return true if is active, exception is catched if its not alive
     * @throws RemoteException
     */
    public boolean tryPing() throws RemoteException;

    public void setIndex(HashMap<String, HashSet<URL>> in) throws RemoteException;

    public void setPath(HashMap<String, HashSet<URL>> in) throws RemoteException;

    public HashMap<String, HashSet<URL>> getIndex() throws RemoteException;

    public HashMap<String, HashSet<URL>> getPath() throws RemoteException;

}
