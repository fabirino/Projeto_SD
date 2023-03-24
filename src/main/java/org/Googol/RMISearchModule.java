package org.Googol;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;

/**
 * <p>
 * Porta de entrada para o sistema
 * <p>
 * O Search Module escolhe um Storage Barrel para responder a cada pesquisa
 * <p>
 * Comunica com o Storage Barrels por RMI
 */
//TODO: SUBSTITUIR ARRAYLIST POR QQ CENA THREAD SAFE , USEI O VECTOR MAS O STOR TINHA DITO QUE JA ESTAVA ULTRAPASSADO SLA
public class RMISearchModule extends UnicastRemoteObject implements GoogolInterface, StorageBarrelInterface {
    static ArrayList<StorageBarrelInterfaceB> listOfBarrels;
    String menu;
    Queue urlQueue;

    public RMISearchModule(int i) throws RemoteException {
        super();
        if (i == 0) {
            menu = """
                    1 - Index URL
                    2 - Pages with word
                    3 - Pages with URL
                    4 - Show Stats
                    0 - Exit
                    """;
            urlQueue = new Queue();
            listOfBarrels = new ArrayList<StorageBarrelInterfaceB>();
        }

    }

    public static void main(String[] args) throws RemoteException {

        GoogolInterface SMi = new RMISearchModule(0);
        try {
            LocateRegistry.createRegistry(1099).rebind("SM", SMi);

        } catch (RemoteException RE) {
            RE.printStackTrace();
        } finally {

        }

        try {
            StorageBarrelInterface SMi2 = new RMISearchModule(1);
            LocateRegistry.createRegistry(1098).rebind("SB", SMi2);
            System.out.println("Hello Server ready.");
        } catch (Exception re) {
            System.out.println("Exception in HelloImpl.main: " + re);
        }
    }

    public void newURL(String URLString) throws RemoteException {
        System.out.println("Search Module: Adding \"" + URLString + "\" to the QUEUE");
        urlQueue.addURL(new URL(URLString));
    }

    public String pagesWithWord(String word) throws RemoteException {
        // public void pagesWithWord(String[] word) throws RemoteException {
        String ret = "";
        if (listOfBarrels.size() == 0) {

            return "\nThere are no active barrels!";
        }
        Random gerador = new Random();
        StorageBarrelInterfaceB Barrel = listOfBarrels.get(gerador.nextInt(listOfBarrels.size()));
        HashSet<URL> hash = Barrel.getUrlsToClient(word);
        ret = "\n";
        if (hash != null) {
            for (URL url : hash) {
                for (String url2 : url.getUrls()) {
                    ret = ret + '\n' + url2;
                }
            }
            return ret;
        } else {
            return "\nThere are no Urls with that word!";
        }

    }

    public Vector<String> pagesWithURL(String URL) throws RemoteException {
        Vector<String> a = new Vector<>();
        if (listOfBarrels.size() == 0) {
            a.add("There are no active barrels!");
            return a;
        }
        Random gerador = new Random();
        StorageBarrelInterfaceB Barrel = listOfBarrels.get(gerador.nextInt(listOfBarrels.size()));
        HashSet<URL> hash = Barrel.getUrlsToClient(URL);
        if (hash != null) {
            for (URL url : hash) {
                for (String url2 : url.getUrls()) {
                    a.add(url2);
                }
            }
            return a;
        } else {
            a.add("There are no Urls with that URL!");
            return a;
        }
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

    public void subscribe(String name, StorageBarrelInterfaceB c) throws RemoteException {
        System.out.println("Subscribing " + name);
        listOfBarrels.add(c);
    }

}
