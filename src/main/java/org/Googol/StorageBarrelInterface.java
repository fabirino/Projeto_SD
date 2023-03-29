package org.Googol;

import java.rmi.*;

public interface StorageBarrelInterface extends Remote{
    public int subscribeB(String name, StorageBarrelInterfaceB client) throws RemoteException;
    public void unsubscribeB(StorageBarrelInterfaceB client) throws RemoteException;
}