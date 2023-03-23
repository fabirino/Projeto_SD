package org.Googol;

import java.rmi.*;
import java.util.HashMap;
import java.util.HashSet;

public interface StorageBarrelInterface extends Remote{
    //TODO:
    public void print_on_server(String s) throws java.rmi.RemoteException;
    public void subscribe(String name, StorageBarrelInterfaceB client) throws RemoteException;
    // public HashSet<URL> getUrlsToClient(String Keyword,HashMap<String, HashSet<URL>> index);
}