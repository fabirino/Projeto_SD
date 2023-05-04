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

    /** 
     * Used by the server when a barrels is active or a barrel dies
     * @param variavel number of barrels
     * @throws RemoteException
     */
    public void setvariavel(int variavel) throws RemoteException;

    /**
     * used to sync the information between barrels
     * @param variavel change to true, and the barrels wait
     * @throws RemoteException
     */
    public void setsyncD(Boolean variavel) throws RemoteException;

}
