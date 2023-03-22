package org.Googol;

import java.io.*;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.HashSet;

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
public class IndexStorageBarrel implements Runnable {
    private HashMap<String, HashSet<String>> barrelMap;
    private File file;
    Thread thread;
    private static String MULTICAST_ADDRESS = "224.3.2.1";
    private static int PORT = 4321;
    private static int bufferSize = 65507; // MAX: 65507

    public IndexStorageBarrel() {
        file = new File("./info\\BARREL.obj");
        this.barrelMap = new HashMap<>();
        onRecovery();
    }

    public static void main(String[] args) {
        IndexStorageBarrel storageBarrel = new IndexStorageBarrel();
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

    public void saveURL(URL url) {

        HashSet<String> urls;

        for (String Keyword : url.getKeywords()) {
            if(barrelMap.containsKey(Keyword)){
                urls = barrelMap.get(Keyword);
                if(!urls.contains(url.getUrl())){
                    urls.add(url.getUrl());
                }
                // barrelMap.put(Keyword, urls);
                barrelMap.replace(Keyword, urls);
            }else{
                urls = new HashSet<>();
                urls.add(url.getUrl());
                barrelMap.put(Keyword, urls);
            }  
        }

        return;
    }

    public void onRecovery() {
        System.out.println("Barrel: System started, pulling last saved Hashmap barrel.");
        if (file.exists() && file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file);
                    ObjectInputStream ois = new ObjectInputStream(fis)) {
                barrelMap = (HashMap<String, HashSet<String>>) ois.readObject();
            } catch (IOException e) {
                System.out.println("Error trying to read \"BARREL.obj\".");
            } catch (ClassNotFoundException e) {
                System.out.println("Class \"BARREL\" not found.");
            }
        }
    }

    public void onCrash() {
        System.out.println("Barrel: System crashed, saving Hashmap barrel.");
        if (barrelMap.size() != 0) {
            try (FileOutputStream fos = new FileOutputStream(file);
                    ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(barrelMap);
            } catch (IOException ioe) {
                System.out.println("Error trying to write to \"BARREL.obj\".");
            }
        }
    }

    public void run() {

    }
}
