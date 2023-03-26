package org.Googol;

import java.rmi.*;

public interface StorageBarrelInterface extends Remote{
    public void subscribe(String name, StorageBarrelInterfaceB client) throws RemoteException;
    public void unsubsribe(StorageBarrelInterfaceB client) throws RemoteException;
}