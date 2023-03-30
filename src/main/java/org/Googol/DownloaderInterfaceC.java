package org.Googol;

import java.rmi.*;

/**
 * Interface implementada pelos Downloaders para existir comunicação bidirecional
 * entre o Search Module e os Downloaders via RMI
 */
public interface DownloaderInterfaceC extends Remote {
    /**
     * Used by the Search Module to know the id of a given Downloader
     * @return the id of the Downloader
     * @throws RemoteException
     */
    public int getId() throws RemoteException;

    /**
     * Used when the Search Module Crashes
     * @throws RemoteException
     */
    public void crashSearchModel() throws RemoteException;

    public void setvariavel(int variavel) throws RemoteException;

}
