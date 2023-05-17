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
    public Response getUrlsToClient(String[] Keywords, int pages) throws RemoteException;

    /**
     * <p> Used to return all the URLs taht lead to a certain URL
     * <p> Results separated by pages of 10 URLs each
     * @param URL URL given by the client
     * @param pages correspondent page of the serach
     * @return
     * @throws RemoteException
     */
    public Response getpagesWithURL(String URL, int pages) throws RemoteException;

    /**
     * Get Method
     * @return id of the Barrel
     * @throws RemoteException
     */
    public int getId() throws RemoteException;

    /**
     * 
     * @return 
     * @throws RemoteException
     */
    public String getIP() throws RemoteException;

    /**
     * Used to verify if a Barrel is still active
     * @return true if is active, exception is catched if its not alive
     * @throws RemoteException
     */
    public boolean tryPing() throws RemoteException;

    /**
     * Set Method
     * Used to set the Path after a sync with other barrels
     * @param in Path to be set
     * @throws RemoteException
     */
    public void setIndex(HashMap<String, HashSet<URL>> in) throws RemoteException;

    /**
     * Set Method
     * Used to set the Index after a sync with other barrels
     * @param in index to be set
     * @throws RemoteException
     */
    public void setPath(HashMap<String, HashSet<URL>> in) throws RemoteException;

    /**
     * Get Method
     * @return Index
     * @throws RemoteException
     */
    public HashMap<String, HashSet<URL>> getIndex() throws RemoteException;

    /**
     * Get Method
     * @return Path
     * @throws RemoteException
     */
    public HashMap<String, HashSet<URL>> getPath() throws RemoteException;

}
