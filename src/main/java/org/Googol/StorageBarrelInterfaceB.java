package org.Googol;

import java.rmi.*;
import java.util.HashSet;

public interface StorageBarrelInterfaceB extends Remote{
    public String getUrlsToClient(String[] Keywords, int pages) throws RemoteException;
    public HashSet<String> getpagesWithURL(String URL, int pages) throws RemoteException;
    public int getId() throws RemoteException;

}
