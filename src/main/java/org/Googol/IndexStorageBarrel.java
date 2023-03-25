package org.Googol;

import java.io.*;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.plaf.synth.SynthScrollBarUI;

/**
 * <p>
 * Servidor Central replicado
 * <p>
 * Recebe palavras e URLs dos downloaders
 * <p>
 * Trabalham em paralelo
 * <p>
 * Esta classe ira guardar a informacao em ficheiro(s) na pasta info
 * <p>
 * Recebe a informacao dos Downloaders por Multicast
 *
 * <p>
 * Comunica com o SearchModule por RMI
 */
public class IndexStorageBarrel extends UnicastRemoteObject implements StorageBarrelInterfaceB {
    private HashMap<String, HashSet<URL>> index; // Palavra: lista de URLs
    private HashMap<String, HashSet<String>> path; // URL: lista de URLs que levam ate ele
    private File file;
    private static String MULTICAST_ADDRESS = "224.3.2.1";
    private static int PORT = 4321;
    private static int bufferSize = 65507; // MAX: 65507
    private static StorageBarrelInterface SBi;

    public IndexStorageBarrel() throws RemoteException {
        super();
        file = new File("./info\\BARREL.obj");
        this.index = new HashMap<>();
        this.path = new HashMap<>();
        onRecovery();
    }

    public static void main(String[] args) {
        IndexStorageBarrel storageBarrel;

        try {
            storageBarrel = new IndexStorageBarrel();

            // Catch Crtl C to save data
            Thread t0 = new Thread("t0") {
                public void run() {
                        storageBarrel.onCrash();
                        System.out.println("Barrel: Shutdown");
                        // storageBarrel.onCrash();
                    try{

                        SBi.unsubsribe((StorageBarrelInterfaceB) storageBarrel);
                    }catch(RemoteException re){
                        re.printStackTrace();
                    }
                }
            };
            Runtime.getRuntime().addShutdownHook(t0);

            // thread usada para o Multicast
            Thread t1 = new Thread(new Runnable() {
                public void run() {
                    MulticastSocket socket = null;
                    try {
                        // create socket and bind it
                        socket = new MulticastSocket(PORT);
                        InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
                        socket.joinGroup(group);

                        while (true) {
                            // Create buffer
                            byte[] buffer = new byte[bufferSize];
                            socket.receive(new DatagramPacket(buffer, bufferSize, group, PORT));

                            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
                            ObjectInputStream ois = new ObjectInputStream(bais);
                            try {
                                Object readObject = ois.readObject();
                                if (readObject instanceof URL) {
                                    URL url = (URL) readObject;
                                    storageBarrel.saveURL(url);
                                    // System.out.println(url);
                                } else {
                                    System.out.println("Barrel: The received object is not of type String!");
                                }
                            } catch (ClassNotFoundException e) {
                                System.out.println("Barrel: Error trying to read from Multicast Socket");
                                storageBarrel.onCrash();
                                return;
                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        socket.close();
                    }
                }
            });

            // Thread usada para comunicar com o server por RMI
            Thread t2 = new Thread(new Runnable() {
                public void run() {

                    try {
                        SBi = (StorageBarrelInterface) Naming.lookup("rmi://localhost:1098/SB");
                        SBi.subscribe("localhost", (StorageBarrelInterfaceB) storageBarrel);
                        System.out.println("Barrel: Subscribed Search Module");

                    } catch (NotBoundException NBE) {
                        System.out.println("System: The interface is not bound");
                        return;
                    } catch (MalformedURLException MFE) {
                        System.out.println("System: The URL specified is malformed");
                    } catch (RemoteException RM) {
                        System.out.println("System: Remote Exception, Search Module might not be running");
                        return;
                    } 
                }

            });
            try {
                t1.start();
                t2.start();
                t1.join();
                t2.join();
            } catch (Exception e) {

            }
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    /**
     * Function that saves the URL in the structures
     * 
     * @param url URL object to be stored
     */
    public void saveURL(URL url) {

        // save in this.index
        HashSet<URL> hashset;
        for (String Keyword : url.getKeywords()) {
            if (index.containsKey(Keyword)) {
                hashset = index.get(Keyword);
                if (!hashset.contains(url)) {
                    hashset.add(url);
                }
                // barrelMap.put(Keyword, urls);
                index.replace(Keyword, hashset);
            } else {
                hashset = new HashSet<>();
                hashset.add(url);
                index.put(Keyword, hashset);
            }
        }

        // save in this.path
        HashSet<String> hashset2;// BUG: nao sei se e bug ou nao mas se conseguires ve se ele esta a guardar bem
                                 // que eu fiz rmi atraves do cliente para mostras isto e nao esta a mandar nada
        for (String u : url.getUrls()) {
            if (path.containsKey(u)) {
                hashset2 = path.get(u);
                if (!hashset2.contains(url.getUrl())) {
                    hashset2.add(url.getUrl());
                }
                path.replace(u, hashset2);
            } else {
                hashset2 = new HashSet<>();
                hashset2.add(u);
                path.put(u, hashset2);
            }
        }
    }

    /**
     * <p>
     * Function used when in the start of the System or after a crash
     * <p>
     * Reads the most recent data from the object file
     */
    public void onRecovery() {
        System.out.println("Barrel: System started, pulling last saved Hashmap barrel.");
        if (file.exists() && file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file);
                    ObjectInputStream ois = new ObjectInputStream(fis)) {
                index = (HashMap<String, HashSet<URL>>) ois.readObject();
                ois.close();
                fis.close();
            } catch (IOException e) {
                System.out.println("Barrel: Error trying to read \"BARREL.obj\".");
            } catch (ClassNotFoundException e) {
                System.out.println("Class \"BARREL\" not found.");
            }
        }

    }

    /**
     * <p>
     * Function used when an exception ocurres
     * <p>
     * It saves the corrunt state of the index in an object file
     * TODO: unsubscribe from Search Module
     */
    public void onCrash() {
        System.out.println("Barrel: System crashed, saving Hashmap barrel.");
        if (index.size() != 0) {
            try (FileOutputStream fos = new FileOutputStream(file);
                    ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(index);
                oos.close();
                fos.close();
            } catch (IOException ioe) {
                System.out.println("Barrel: Error trying to write to \"BARREL.obj\".");
            }
        }
    }

    /**
     * Function used when a client search for a set of words
     * 
     * @param Keywords Words specified by the client for the search
     * @param pages    set of pages that will be sent to the client
     * @return Hashset of urls containing the {Keyword(s)} specified by the client
     */
    public HashSet<URL> getUrlsToClient(String[] Keywords, int pages) throws RemoteException {
        // Uses pagesWithWord
        System.out.println("Barrel: Sending URLs that contain the words " + Keywords[0]);
        HashSet<URL> set = new HashSet<>();
        boolean existe = false;
        for (String s : Keywords) {
            if (index.containsKey(s)) {
                existe = true;
                System.out.println("adding" + index.get(s));
                set.addAll(index.get(s));
            }
        }

        // only send 10 pages
        int min = pages * 10;
        int max = min + 10;
        int count = 0;
        Iterator<URL> it = set.iterator();
        HashSet<URL> set2 = new HashSet<>();
        System.out.println(set);

        while (it.hasNext()) {
            if (count >= min && count < max) {
                System.out.println("URL n" + count);
                set2.add(it.next());
                if (min++ == max)
                    break;
            }
            count++;
        }
        if (existe)
            return set2;
        else
            return null;
    }

    /**
     * Function used when a client asks for what URLs lead to a certain URL
     * TODO: mudar o nome da funcao que me confunde todo hahahah
     * 
     * @param URL   URL specified by the user
     * @param pages set of pages that will be sent to the client
     * @return
     */
    public HashSet<String> getpagesWithURL(String URL, int pages) throws RemoteException {
        // Uses pagesWithULR
        System.out.println("Barrel: Sending URLs that lead to " + URL);
        if (path.containsKey(URL)) {
            return path.get(URL);
        } else {
            return null;
        }
    }
}
