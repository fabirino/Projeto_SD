package org.Googol;

import java.rmi.*;
import java.util.HashMap;
import java.util.HashSet;

public interface StorageBarrelInterface extends Remote{
    //TODO:
    public void subscribe(String name, StorageBarrelInterfaceB client) throws RemoteException;
    public void unsubsribe(StorageBarrelInterfaceB client) throws RemoteException;
}