package org.Googol;

import java.rmi.*;

public interface DownloaderInterfaceC extends Remote {
    public int getId() throws RemoteException;

    public void crashSearchModel() throws RemoteException;
}
