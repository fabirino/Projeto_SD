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
    private File file;
    private static String MULTICAST_ADDRESS = "224.3.2.1";
    private static int PORT = 4321;
    private static int bufferSize = 65507; // MAX: 65507

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
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    storageBarrel.onCrash();
                    System.out.println("Barrel: Shutdown");
                    // storageBarrel.onCrash();
                }
            });

            Thread t1 = new Thread(new Runnable() { // thread para
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
                                    System.out.println(url);
                                } else {
                                    System.out.println("The received object is not of type String!");
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

            Thread t2 = new Thread(new Runnable() { // para comunicar com o server por RMI
                public void run() {
                    StorageBarrelInterface SBi;
                    try {
                        SBi = (StorageBarrelInterface) Naming.lookup("rmi://localhost:1098/SB");
                        SBi.subscribe("localhost", (StorageBarrelInterfaceB) storageBarrel);
                        System.out.println("Client sent subscription to server");

                        // TODO: substituir os returns por algo sustentavel
                    } catch (NotBoundException NBE) {
                        System.out.println("System: The interface is not bound");
                        return;
                    } catch (MalformedURLException MFE) {
                        System.out.println("System: The URL specified is malformed");
                        return;
                    } catch (RemoteException RM) {
                        System.out.println("System: Remote Exception catched");
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
        HashSet<String> hashset2;
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

    public void onRecovery() {
        System.out.println("Barrel: System started, pulling last saved Hashmap barrel.");
        if (file.exists() && file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file);
                    ObjectInputStream ois = new ObjectInputStream(fis)) {
                index = (HashMap<String, HashSet<URL>>) ois.readObject();
            } catch (IOException e) {
                System.out.println("Error trying to read \"BARREL.obj\".");
            } catch (ClassNotFoundException e) {
                System.out.println("Class \"BARREL\" not found.");
            }
        }

    }

    public void onCrash() {
        System.out.println("Barrel: System crashed, saving Hashmap barrel.");
        if (index.size() != 0) {
            try (FileOutputStream fos = new FileOutputStream(file);
                    ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(index);
            } catch (IOException ioe) {
                System.out.println("Error trying to write to \"BARREL.obj\".");
            }
        }
    }

    public HashSet<URL> getUrlsToClient(String Keyword) throws RemoteException{
        if (index.containsKey(Keyword)) {
            return index.get(Keyword);
        } else {
            return null;
        }

    }
}
