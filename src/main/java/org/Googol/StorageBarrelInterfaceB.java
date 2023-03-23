package org.Googol;

import java.rmi.*;
import java.util.HashMap;
import java.util.HashSet;

public interface StorageBarrelInterfaceB extends Remote{
    public HashSet<URL> getUrlsToClient(String Keyword) throws RemoteException;

}
