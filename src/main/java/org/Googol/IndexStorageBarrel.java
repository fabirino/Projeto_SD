package org.Googol;

import java.io.*;
import java.io.FileInputStream;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;

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
    private File fileIndex;
    private File filePath;
    private final static String MULTICAST_ADDRESS = "224.3.2.1";
    private final static int PORT = 4321;
    private final static int bufferSize = 65507; // MAX: 65507
    private static StorageBarrelInterface SBi;
    private int id;

    public IndexStorageBarrel() throws RemoteException {
        super();
        fileIndex = new File("./info\\INDEX_" + id + ".obj");
        filePath = new File("./info\\PATH_" + id + ".obj");
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
                    System.out.println("Barrel: Shutdown");
                    storageBarrel.onCrash();
                    try {

                        SBi.unsubscribeB((StorageBarrelInterfaceB) storageBarrel);
                    } catch (RemoteException re) {
                        re.printStackTrace();
                    }catch (ConcurrentModificationException e){
                        System.out.println("Barrel: Error trying to write to a Hashmap");
                        storageBarrel.onCrash();
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
                                } else {
                                    System.out.println("Barrel: The received object is not of type String!");
                                }
                            } catch (ClassNotFoundException e) {
                                System.out.println("Barrel: Error trying to read from Multicast Socket");
                                storageBarrel.onCrash();
                                return;
                            }catch (ConcurrentModificationException e){
                                System.out.println("Barrel: Error trying to write to a Hashmap");
                                storageBarrel.onCrash();
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
                        int num = SBi.subscribeB("localhost", (StorageBarrelInterfaceB) storageBarrel);
                        storageBarrel.setId(num);
                        System.out.println("Barrel: Subscribed Search Module");

                    } catch (NotBoundException NBE) {
                        System.out.println("System: The interface is not bound");
                        return;
                    } catch (MalformedURLException MFE) {
                        System.out.println("System: The URL specified is malformed");
                    } catch (RemoteException RM) {
                        System.out.println("System: Remote Exception, Search Module might not be running");
                        return;
                    }catch (ConcurrentModificationException e){
                        System.out.println("Barrel: Error trying to write to a Hashmap");
                        storageBarrel.onCrash();
                    }
                }

            });
            try {
                t1.start();
                t2.start();
                t1.join();
                t2.join();
            } catch (InterruptedException e) {
                System.out.println("Barrel: Something went wrong with the threads :/");
            }
            
        } catch (RemoteException e) {
            System.out.println("Barrel: The Search Module not responding");
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
                    index.replace(Keyword, hashset);
                }

            } else {
                hashset = new HashSet<>();
                hashset.add(url);
                index.put(Keyword, hashset);
            }
        }

        // save in this.path
        HashSet<String> hashset2;

        for (String u : url.getUrls()) {
            if (path.containsKey(u)) {
                hashset2 = path.get(u);
                if (!hashset2.contains(url.getUrl())) {
                    hashset2.add(url.getUrl());
                    path.replace(u, hashset2);
                }
            } else {
                hashset2 = new HashSet<>();
                hashset2.add(url.getUrl());
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
        // Recovering index
        if (fileIndex.exists() && fileIndex.isFile()) {
            try (FileInputStream fis = new FileInputStream(fileIndex);
                    ObjectInputStream ois = new ObjectInputStream(fis)) {
                index = (HashMap<String, HashSet<URL>>) ois.readObject();
                ois.close();
                fis.close();
            } catch (IOException e) {
                System.out.println("Barrel: Error trying to read \"INDEX_" + id + ".obj\".");
            } catch (ClassNotFoundException e) {
                System.out.println("Class \"BARREL\" not found.");
            }
        }

        // Recovering path
        if (filePath.exists() && filePath.isFile()) {
            try (FileInputStream fis = new FileInputStream(filePath);
                    ObjectInputStream ois = new ObjectInputStream(fis)) {
                path = (HashMap<String, HashSet<String>>) ois.readObject();
                ois.close();
                fis.close();
            } catch (IOException e) {
                System.out.println("Barrel: Error trying to read \"PATH_" + id + ".obj\".");
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
            try (FileOutputStream fos = new FileOutputStream(fileIndex);
                    ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(index);
                oos.close();
                fos.close();
            } catch (IOException ioe) {
                System.out.println("Barrel: Error trying to write to \"INDEX_" + id + ".obj\".");
            }
        }

        if (path.size() != 0) {
            try (FileOutputStream fos = new FileOutputStream(filePath);
                    ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(path);
                oos.close();
                fos.close();
            } catch (IOException ioe) {
                System.out.println("Barrel: Error trying to write to \"PATH_" + id + ".obj\".");
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

        for (String s : Keywords) {
            if (index.containsKey(s)) {
                System.out.println("adding" + index.get(s));

                set.addAll(index.get(s));
                System.out.println(set.size() + s);
            }
        }
        System.out.println("size set -> " + set.size());
        // only send 10 pages
        int min = pages * 10;
        int max = min + 10;
        int count = 0;
        Iterator<URL> it = set.iterator();
        HashSet<URL> set2 = new HashSet<>();

        for (URL url : set) {
            count++;
            if (count >= min && count < max) {
                System.out.println("URL n" + count);
                set2.add(it.next());
            } else if (count >= max) {
                break;
            }
        }
        /*
         * while (it.hasNext()) {
         * System.out.println(count);
         * if (count >= min && count < max) {
         * System.out.println("URL n" + count);
         * set2.add(it.next());
         * } else if (count >= max) {
         * break;
         * }
         * count++;
         * }
         */
        if (set2.size() != 0)
            return set2;
        else
            return null;
    }

    /**
     * Function used when a client asks for what URLs lead to a certain URL
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

    public int getId() throws RemoteException{
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
