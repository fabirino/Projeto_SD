package org.Googol;

import java.io.*;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
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
    private static String name;

    public IndexStorageBarrel() throws RemoteException {
        super();
        fileIndex = new File("./info\\INDEX_" + name + ".obj");
        filePath = new File("./info\\PATH_" + name + ".obj");
        this.index = new HashMap<>();
        this.path = new HashMap<>();
        onRecovery();
    }

    public static void main(String[] args) {
        IndexStorageBarrel storageBarrel;

        try {
            Scanner scan = new Scanner(System.in);
            System.out.print("Name of Barrel >> ");
            name = scan.nextLine();
            scan.close();
            storageBarrel = new IndexStorageBarrel();

            // Catch Crtl C to save data
            Thread t0 = new Thread("t0") {
                public void run() {
                    storageBarrel.onCrash();
                    System.out.println("Barrel: Shutdown");
                    try {

                        SBi.unsubscribeB((StorageBarrelInterfaceB) storageBarrel);
                    } catch (RemoteException re) {
                        re.printStackTrace();
                    } catch (ConcurrentModificationException e) {
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

                        try (DatagramSocket aSocket = new DatagramSocket()) {
                            while (true) {
                                // Create buffer
                                byte[] buffer = new byte[bufferSize];
                                socket.receive(new DatagramPacket(buffer, bufferSize, group, PORT));

                                ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
                                ObjectInputStream ois = new ObjectInputStream(bais);
                                try {
                                    Object readObject = ois.readObject();
                                    if (readObject instanceof Message) {
                                        Message m = (Message) readObject;
                                        URL url = m.getURL();
                                        String texto = "recebi" + name + "\n";
                                        byte[] me = texto.getBytes();

                                        InetAddress aHost = InetAddress.getByName("localhost");//DEBUG: para ser na mm maquina
                                        DatagramPacket request = new DatagramPacket(me, me.length, aHost, m.getPORT());
                                        aSocket.send(request);

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
                        } catch (SocketException e) {
                            System.out.println("Socket: " + e.getMessage());
                        } catch (IOException e) {
                            System.out.println("IO: " + e.getMessage());
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
                    } catch (ConcurrentModificationException e) {
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
     * Function that saves the URL in the structures index and path
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
     * <p> Function used when a client search for a set of words
     * <p> The search is ordered by relevance and its separated by pages of 10 URLs each
     * 
     * @param Keywords Words specified by the client for the search
     * @param pages    set of pages that will be sent to the client
     * @return String containing the URLs that contain the {@code Keyword(s)} specified by the client
     */
    public String getUrlsToClient(String[] Keywords, int pages) throws RemoteException {
        // Uses pagesWithWord
        System.out.println("Barrel: Sending URLs that contain the words " + Keywords[0] + "...");
        HashSet<URL> commonValues = new HashSet<>();

        for (int i = 0; i < Keywords.length; i++) {
            if (index.containsKey(Keywords[i])) {
                HashSet<URL> values = index.get(Keywords[i]);
                if (i == 0) {
                    commonValues.addAll(values);
                } else {
                    commonValues.retainAll(values);
                }
            }
        }
        
        // Order the results by relevance
        ArrayList<Relevance> ordered = new ArrayList<>();
        for(URL url: commonValues){
            Relevance aux = new Relevance(url, path.get(url.getUrl()).size());
            ordered.add(aux);
        }
        // reverse order
        Collections.sort(ordered, new Comparator<Relevance>() {
            @Override
            public int compare(Relevance r1, Relevance r2) {
                return r2.getRelevance() - r1.getRelevance();
            }
        });

        
        // only send 10 pages 
        String result = "";
        int count = 0;
        int min = pages * 10;
        int max = min + 10;
        
        for (Relevance rel : ordered) {
            // System.out.println(rel.getRelevance());
            // System.out.println(rel.getURL());
            count++;
            if (count >= min && count < max) {
                result += rel.getURL() + '\n';
            } else if (count >=max){
                break;
            }
        }

        if (result != "")
            return result;
        else
            return null;
    }

    /**
     * <p> Function used when a client asks for what URLs lead to a certain URL
     * <p> The result is separeted by pages of 10 URLs each
     * 
     * @param URL   URL specified by the user
     * @param pages set of pages that will be sent to the client
     * @return Hashset containing the 10 URLs of the page
     */
    public HashSet<String> getpagesWithURL(String URL, int pages) throws RemoteException {
        // Uses pagesWithULR
        System.out.println("Barrel: Sending URLs that lead to " + URL);
        HashSet<String> set = new HashSet<>();

        if (path.containsKey(URL)) {
            System.out.println("adding" + path.get(URL));

            set.addAll(path.get(URL));
            System.out.println(set.size() + URL);
        }

        // only send 10 pages
        int min = pages * 10;
        int max = min + 10;
        int count = 0;
        HashSet<String> set2 = new HashSet<>();
        // System.out.println(set);

        for (String url : set) {
            count++;
            if (count >= min && count < max) {
                System.out.println("URL n" + count);
                set2.add(url);
            } else if (count >= max) {
                break;
            }
        }
        if (set2.size() != 0)
            return set2;
        else
            return null;
    }

    public int getId() throws RemoteException {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
