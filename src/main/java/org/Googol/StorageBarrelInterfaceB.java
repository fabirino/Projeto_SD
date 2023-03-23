package org.Googol;

import java.rmi.*;

public interface StorageBarrelInterfaceB extends Remote{
	public void print_on_client(String s) throws java.rmi.RemoteException;
}
