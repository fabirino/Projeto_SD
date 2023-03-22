package org.Googol;

import java.io.*;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <p> Servidor Central replicado
 * <p> Recebe palavras e URLs dos downloaders
 * <p> Trabalham em paralelo
 * <p> Esta classe ira guardar a informacao em ficheiro(s) na pasta info
 * <p> Recebe a informacao dos Downloaders por Multicast
 *
 * <p> Comunica com o SearchModule por RMI
 */

//TODO: HASH MAP aqui
public class IndexStorageBarrel implements Runnable {
    private HashSet<URL> urls;
    Thread thread;

    public IndexStorageBarrel() {
        if()
        this.urls = urls;
    }

    public void onRecovery() {
        System.out.println("Barrel: System started, pulling last saved Hashset queue.");
//        if (file.exists() && file.isFile()) {
//            try (FileInputStream fis = new FileInputStream(file);
//                 ObjectInputStream ois = new ObjectInputStream(fis)) {
//                queue = (LinkedBlockingQueue<URL>) ois.readObject();
//            } catch (IOException e) {
//                System.out.println("Error trying to read \"QUEUE.obj\".");
//            } catch (ClassNotFoundException e) {
//                System.out.println("Class \"QUEUE\" not found.");
//            }
//        }
    }

    public void onCrash() {
        System.out.println("Barrel: System crashed, saving Hashset state.");
//        if (queue.size() != 0) {
//            try (FileOutputStream fos = new FileOutputStream(file);
//                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
//                oos.writeObject(queue);
//            } catch (IOException ioe) {
//                System.out.println("Error trying to write to \"QUEUE.obj\".");
//            }
        }
    }

    public void run(){

    }
}
