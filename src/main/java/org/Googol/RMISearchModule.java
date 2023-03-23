package org.Googol;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

/**
 * <p> Porta de entrada para o sistema
 * <p> O Search Module escolhe um Storage Barrel para responder a cada pesquisa
 * <p> Comunica com o Storage Barrels por RMI
 */
public class RMISearchModule extends UnicastRemoteObject implements GoogolInterface,StorageBarrelInterface {
    static ArrayList<StorageBarrelInterfaceB> listOfBarrels;
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
        listOfBarrels = new ArrayList<StorageBarrelInterfaceB>();
    }

    public static void main(String[] args) throws RemoteException {

        GoogolInterface SMi = new RMISearchModule();
        try {
            LocateRegistry.createRegistry(1099).rebind("SM", SMi);

        } catch (RemoteException RE) {
            RE.printStackTrace();
        } finally {

        }

        Thread t1 = new Thread(new Runnable() { // para comunicar com o server por RMI
            public void run() {
                String a;
                try (Scanner sc = new Scanner(System.in)) {
                    //User user = new User();
                    GoogolInterface SMi2 = new RMISearchModule();
                    LocateRegistry.createRegistry(1098).rebind("SB", SMi2);
                    System.out.println("Hello Server ready.");
                    while (true) {
                        System.out.print("> ");
                        a = sc.nextLine();
                        for(StorageBarrelInterfaceB barrel : listOfBarrels){
                            barrel.print_on_client(a);
                        }
                    }
                } catch (Exception re) {
                    System.out.println("Exception in HelloImpl.main: " + re);
                } 
            }

        });
        try {
            t1.start();
            t1.join();
        } catch (Exception e) {

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
    //====================================================
    public void print_on_server(String s) throws RemoteException {
		System.out.println("> " + s);
	}

	public void subscribe(String name, StorageBarrelInterfaceB c) throws RemoteException {
		System.out.println("Subscribing " + name);
		System.out.print("> ");
		listOfBarrels.add(c);
	}

    // public HashSet<URL> getUrlsToClient(String Keyword,HashMap<String, HashSet<URL>> index){
    //     if(index.containsKey(Keyword)){
    //         return index.get(Keyword);
    //     }
    //     else{
    //         return null;
    //     }
        
    // }

}

