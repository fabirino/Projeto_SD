package org.Googol;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

/**
 * <p> Porta de entrada para o sistema
 * <p> O Search Module escolhe um Storage Barrel para responder a cada pesquisa
 * <p> Comunica com o Storage Barrels por RMI
 */
public class RMISearchModule extends UnicastRemoteObject implements GoogolInterface {

    String menu;
    Queue urlQueue;

    public RMISearchModule() throws RemoteException {
        super();
        menu = """
                1 - Index URL
                2 - Pages with word
                3 - Pages with URL
                4 - Show Stats
                0 - Exit
                """;
        urlQueue = new Queue();
    }

    public static void main(String[] args) throws RemoteException {

        GoogolInterface SMi = new RMISearchModule();
        try {
            LocateRegistry.createRegistry(1099).rebind("SM", SMi);

        } catch (RemoteException RE) {
            RE.printStackTrace();
        } finally {

        }
    }

    public void newURL(String URLString) throws RemoteException {
        System.out.println("Search Module: Adding \"" + URLString + "\" to the QUEUE");
        urlQueue.addURL(new URL(URLString));
    }


    public void pagesWithWord(String[] word) throws RemoteException {

    }

    public void pagesWithURL(String URL) throws RemoteException {

    }

    public void adminPage() throws RemoteException {

    }

    public String menu() throws RemoteException {
        return menu;
    }

    // QUEUE Functions ======================================================
    public boolean addURLQueue(URL URL) throws RemoteException {
        return urlQueue.addURL(URL);
    }

    public URL getURLQueue() throws RemoteException, InterruptedException {
        return urlQueue.getUrl();
    }

}

