package org.Googol;

import java.rmi.*;

public interface StorageBarrelInterface extends Remote{
    public void subscribeI(String name, StorageBarrelInterfaceB client) throws RemoteException;
    public void unsubscribeI(StorageBarrelInterfaceB client) throws RemoteException;
}