package org.Googol;

import java.rmi.*;

/**
 * Interface usada pelo Search Module para adicionar Downloaders à lista de Downloaders
 */
public interface DownloaderInterface extends Remote {

    /**
     * Used when a Downloader starts to subscribe to the list of Downloaders present in the Search Module 
     * @param client Interface used by the Downloaders
     * @return if returns 0, there is no active Barrel, else returns the id of the Downloader
     * @throws RemoteException
     */
    public int subscribeD(DownloaderInterfaceC client) throws RemoteException;

    
    /**
     * Used when a Downloader crashes or ends to unsubscribe the list of Downloaders present in the Search Module 
     * @param client Interface used by the Downloaders
     * @throws RemoteException
     */
    public void unsubscribeD(DownloaderInterfaceC client) throws RemoteException;

    /**
     * Used by a Downloader to get the next URL to be processed 
     * @return next URL to index
     * @throws RemoteException
     */
    public URL getURLQueue() throws RemoteException, InterruptedException;

    /**
     * Used by a Downloader to adds a URL to the Queue
     * @param url to add
     * @return sucess
     * @throws RemoteException
     */
    public boolean addURLQueue(URL URL) throws RemoteException;

    /**
     * Used by the Downloaders to know how many active Barrels there are, and then know how 
     * many      it needs to receive in Multicast
     * @return number of active Barrels
     * @throws RemoteException
     */
    public int getNBarrels() throws RemoteException;

    /**
     * used to check if all barrels are alive, if not, removes it from the list of barrels
     * @throws RemoteException
     */
    public void pingBarrels() throws RemoteException;

}
