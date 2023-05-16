package org.Googol;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ControllerInterface extends Remote {
    
    /**
     * Sends a message to the server's "/app/stats-update" destination
     * @param searches
     * @return true if the message was sent successfully, false otherwise
     */
    public boolean sendMessage(String[] searches) throws RemoteException;

}
