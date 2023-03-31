package org.Googol;

import java.rmi.*;
import java.util.HashMap;
import java.util.HashSet;

public interface StorageBarrelInterface extends Remote{
    /**
     * Used when a Barrel starts to subscribe to the list of Barrels present in the Search Module 
     * @param name String identifier of the Barrel
     * @param client Interface used by the Barrel
     * @return returns the id of the Barrel
     * @throws RemoteException
     */
    public int subscribeB(String name, StorageBarrelInterfaceB client) throws RemoteException;
    
    /**
     * Used when a Barrel crashes or ends to unsubscribe the list of Barrel present in the Search Module 
     * @param client Interface used by the Barrel
     * @throws RemoteException
     */
    public void unsubscribeB(StorageBarrelInterfaceB client) throws RemoteException;

    /**
     * used to syncronize
     * @throws RemoteException
     */
    public void updatesyncD() throws RemoteException;

    /**
     * Used to sync the information of the barrels if a barrel starts after the another, so they have similar information
     * @param c barrel
     * @param index data
     * @return hashset
     * @throws RemoteException
     */
    public HashMap<String, HashSet<URL>> syncIndex(StorageBarrelInterfaceB c ,HashMap<String, HashSet<URL>> index) throws RemoteException; 

    /**
     * Used to sync the information of the barrels if a barrel starts after the another, so they have similar information
     * @param c barrel
     * @param path data
     * @return hashset
     * @throws RemoteException
     */
    public HashMap<String, HashSet<URL>> syncPath(StorageBarrelInterfaceB c ,HashMap<String, HashSet<URL>> path) throws RemoteException;

}