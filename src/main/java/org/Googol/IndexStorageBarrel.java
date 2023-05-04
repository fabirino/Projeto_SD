package org.Googol;

import java.io.*;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

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
    private HashMap<String, HashSet<URL>> path; // URL: lista de URLs que levam ate ele
    private File fileIndex;
    private File filePath;
    private final static String MULTICAST_ADDRESS = "224.3.2.1";
    private final static int PORT = 4321;
    private final static int bufferSize = 65536; // MAX: 65507
    private static StorageBarrelInterface SBi;
    private int id;
    private static String name;
    private static String ipServer;

    public IndexStorageBarrel() throws RemoteException {
        super();
        fileIndex = new File("./info\\INDEX_" + name + ".obj");
        filePath = new File("./info\\PATH_" + name + ".obj");
        this.index = new HashMap<>();
        this.path = new HashMap<>();
        onRecovery();
    }

    public static void main(String[] args) {

        if (args.length == 1) {
            ipServer = args[0];
        } else {
            ipServer = "";
        }
        String test = "rmi://" + ipServer + ":1099/SM";

        IndexStorageBarrel storageBarrel;

        try {
            Scanner scan = new Scanner(System.in);
            System.out.print("Name of Barrel >> ");
            name = scan.nextLine();
            scan.close();
            storageBarrel = new IndexStorageBarrel();
            try {
                if (ipServer.equals("")) {
                    SBi = (StorageBarrelInterface) Naming.lookup("rmi://localhost:1098/SB");
                } else {
                    SBi = (StorageBarrelInterface) Naming.lookup(test);// DEBUG: out off machine
                }
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
                storageBarrel.onCrash(1);
            }

            // #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=
            // Sync files obj
            try {
                Thread.sleep(5000);// aguarda que sincronize os outros servers tds
            } catch (Exception e) {
            }
            HashMap<String, HashSet<URL>> hash = SBi.syncIndex((StorageBarrelInterfaceB) storageBarrel,
                    storageBarrel.index);
            if (hash != null) {
                storageBarrel.index = hash;
            }
            hash = SBi.syncPath((StorageBarrelInterfaceB) storageBarrel, storageBarrel.path);
            if (hash != null) {
                storageBarrel.path = hash;
            }
            try {
                Thread.sleep(1000);// aguarda que sincronize os outros servers tds
            } catch (Exception e) {
            }
            SBi.updatesyncD();

            // #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=

            // Catch Crtl C to save data
            Thread t0 = new Thread("t0") {
                public void run() {
                    try {
                        SBi.unsubscribeB((StorageBarrelInterfaceB) storageBarrel);
                    } catch (RemoteException re) {
                        System.out.println("Barrel: The Search Module is no longer running");
                        // re.printStackTrace();
                    } catch (ConcurrentModificationException e) {
                        System.out.println("Barrel: Error trying to write to a Hashmap");
                    } finally {
                        storageBarrel.onCrash(1);
                        System.out.println("Barrel: Shutdown");
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
                            int count = 0;
                            while (true) {
                                // Create buffer
                                byte[] buffer = new byte[bufferSize];
                                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                                socket.receive(packet);

                                byte[] data = Arrays.copyOf(packet.getData(), packet.getLength());

                                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                                GZIPInputStream gzis = new GZIPInputStream(bais);
                                ObjectInputStream ois = new ObjectInputStream(gzis);
                                try {
                                    Object readObject = ois.readObject();
                                    if (readObject instanceof Message) {
                                        Message m = (Message) readObject;
                                        URL url = m.getURL();
                                        String texto = "recebi" + name + "\n";
                                        byte[] me = texto.getBytes();

                                        InetAddress aHost = InetAddress.getByName("localhost");
                                        // InetAddress aHost = InetAddress.getByName(m.getIP());// DEBUG: out off
                                        // machine
                                        DatagramPacket request = new DatagramPacket(me, me.length, aHost, m.getPORT());
                                        aSocket.send(request);

                                        storageBarrel.saveURL(url);
                                        // System.out.println(url);
                                        if (++count == 10) {
                                            storageBarrel.onCrash(0);
                                            count = 0;
                                        }
                                    } else {
                                        System.out.println("Barrel: The received object is not of type String!");
                                    }
                                } catch (ClassNotFoundException e) {
                                    System.out.println("Barrel: Error trying to read from Multicast Socket");
                                    storageBarrel.onCrash(1);
                                    return;
                                }

                            }
                        } catch (SocketException e) {
                            System.out.println("Socket: " + e.getMessage());
                        } catch (IOException e) {
                            System.out.println("IO: " + e.getMessage());
                        }
                    } catch (RemoteException e) {
                        System.out.println("Barrel: The Search Module is no longer running");
                    } catch (UnknownHostException e) {
                        System.out.println("Barrel: The Host does not exist");
                    } catch (IOException e) {
                        System.out.println("Barrel: Error creating Multicast Socket");
                    } finally {
                        socket.close();
                    }
                }
            });

            try {
                t1.start();
                t1.join();
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
        String strurl = url.getUrl();
        for (String Keyword : url.getKeywords()) {
            if (index.containsKey(Keyword)) {
                hashset = index.get(Keyword);
                if (hashset.contains(url)) {
                    // System.out.println("Barrel: URL already exists");
                    continue;
                } else {
                    boolean contains = false;
                    for (URL url2 : hashset) {
                        if (url2.getUrl().equals(strurl)) {
                            contains = true;
                            break;
                        }
                    }
                    if (!contains) {
                        hashset.add(url);
                        index.replace(Keyword, hashset);
                        // System.out.println("DEBUG:" + Keyword + " :" + hashset);
                    }
                }

            } else {
                hashset = new HashSet<>();
                hashset.add(url);
                index.put(Keyword, hashset);
            }
        }

        // save in this.path
        HashSet<URL> hashset2;

        for (String u : url.getUrls()) {
            if (path.containsKey(u)) {
                hashset2 = path.get(u);
                if (hashset2.contains(url)) {
                    // System.out.println("Barrel: URL already exists");
                    continue;
                } else {
                    boolean contains = false;
                    for (URL url2 : hashset2) {
                        if (url2.getUrl().equals(strurl)) {
                            contains = true;
                            break;
                        }
                    }
                    if (!contains) {
                        hashset2.add(url);
                        index.replace(u, hashset2);
                        // System.out.println("DEBUG2:"+ u + " :" + hashset2);
                    }
                }

            } else {
                hashset2 = new HashSet<>();
                hashset2.add(url);
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
                path = (HashMap<String, HashSet<URL>>) ois.readObject();
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
     * 
     * @param print used to print if a crash occures
     */
    public void onCrash(int print) {
        if (print == 1)
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
     * <p>
     * Function used when a client search for a set of words
     * <p>
     * The search is ordered by relevance and its separated by pages of 10 URLs each
     * 
     * @param Keywords Words specified by the client for the search
     * @param pages    set of pages that will be sent to the client
     * @return String containing the URLs that contain the {@code Keyword(s)}
     *         specified by the client
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
        for (URL url : commonValues) {
            if (path.get(url.getUrl()) != null) {
                Relevance aux = new Relevance(url, path.get(url.getUrl()).size());
                ordered.add(aux);
            }
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
            } else if (count >= max) {
                break;
            }
        }

        if (result != "")
            return result;
        else
            return null;
    }

    /**
     * <p>
     * Function used when a client asks for what URLs lead to a certain URL
     * <p>
     * The result is separeted by pages of 10 URLs each
     * 
     * @param URL   URL specified by the user
     * @param pages set of pages that will be sent to the client
     * @return Hashset containing the 10 URLs of the page
     */
    public HashSet<URL> getpagesWithURL(String URL, int pages) throws RemoteException {
        // Uses pagesWithULR
        System.out.println("Barrel: Sending URLs that lead to " + URL);
        HashSet<URL> set = new HashSet<>();

        if (path.containsKey(URL)) {
            System.out.println("adding" + path.get(URL));

            set.addAll(path.get(URL));
            System.out.println(set.size() + URL);
        }

        // only send 10 pages
        int min = pages * 10;
        int max = min + 10;
        int count = 0;
        HashSet<URL> set2 = new HashSet<>();
        // System.out.println(set);

        for (URL url : set) {
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

    /**
     * Get Method
     * 
     * @throws RemoteException
     */
    public int getId() throws RemoteException {
        return id;
    }

    /**
     * Set method
     * 
     * @param id id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    public void setIndex(HashMap<String, HashSet<URL>> in) throws RemoteException {
        this.index = in;
        System.out.println("size index>> " + this.index.size());// APENAS PARA DEBUG!!
    }

    public void setPath(HashMap<String, HashSet<URL>> in) throws RemoteException {
        this.path = in;
        System.out.println("size path >> " + this.path.size());// APENAS PARA DEBUG!!
    }

    public boolean tryPing() throws RemoteException {
        return true;
    }

    public HashMap<String, HashSet<URL>> getIndex() throws RemoteException {
        return this.index;
    }

    /**
     * Get Method
     * 
     */
    public HashMap<String, HashSet<URL>> getPath() throws RemoteException {
        return this.path;
    }

}
