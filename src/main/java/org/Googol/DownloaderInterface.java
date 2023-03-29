package org.Googol;

import java.rmi.*;

public interface DownloaderInterface extends Remote {
    public int subscribeD(DownloaderInterfaceC client) throws RemoteException;

    public void unsubscribeD(DownloaderInterfaceC client) throws RemoteException;

     /**
     * Gets the next URL to be processed
     * @return next URL to index
     * @throws RemoteException
     */
    public URL getURLQueue() throws RemoteException, InterruptedException;

        /**
     * Adds a URL to the Queue
     * @param url to add
     * @return sucess
     * @throws RemoteException
     */
    public boolean addURLQueue(URL URL) throws RemoteException;

     /**
     * 
     * @return
     * @throws RemoteException
     */
    public int getNBarrels() throws RemoteException;

}
