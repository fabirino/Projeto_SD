package org.Googol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.LinkedBlockingDeque;

public class Queue implements Serializable {

    private File file;
    private LinkedBlockingDeque<URL> queue;

    public Queue() {
        file = new File("./info\\QUEUE.obj");
        this.queue = new LinkedBlockingDeque<>();
        onRecovery();
    }

    public static void main(String[] args) {
        System.out.println("Queue: Started");
    }

    /**
     * Adds an URL to the end of the Dequeue
     * 
     * @param url that is added to the queue
     * @return success
     */
    public boolean addURL(URL url) {
        if (!queue.contains(url)) {
            // System.out.println("Queue: Adding " + url.getUrl() + "to the queue");
            return queue.add(url);
        } else
            return false;
    }

    /**
     * Adds an URL to the head of the Dequeue
     * 
     * @param url that is added to the queue
     * @return success
     */
    public boolean addURLHead(URL url) {
        if (!queue.contains(url)) {
            System.out.println("Queue: Adding " + url.getUrl() + " to the queue");
            try {
                queue.addFirst(url);
                return true;
            } catch (Exception e) {
                return false;
            }
        } else
            return false;
    }

    /**
     * Get the last URL from the queue
     *
     * @return last URL
     * @throws InterruptedException
     */
    public URL getUrl() throws InterruptedException {
        return queue.take();
    }

    /**
     * Serialize queue into file in case of a crash or in the end of a program
     * session
     */
    public void onCrash() {
        System.out.println("Queue: System crashed, saving URL queue state.");
        if (queue.size() != 0) {
            try (FileOutputStream fos = new FileOutputStream(file);
                    ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(queue);
                oos.close();
                fos.close();
            } catch (IOException ioe) {
                System.out.println("Error trying to write to \"QUEUE.obj\".");
            }
        }
    }

    /**
     * In the start of the program, if there is a queue from another session, it is
     * recovered, else the queue is empty
     * In case of a crash, if recovery file exists the queue is recovered from a
     * object file
     */
    public void onRecovery() {
        if (file.exists() && file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file);
                    ObjectInputStream ois = new ObjectInputStream(fis)) {
                queue = (LinkedBlockingDeque<URL>) ois.readObject();
                if (queue.size() == 0) {
                    System.out.println("Queue: Queue started empty, there was no data to read.");
                } else {
                    System.out.println("Queue: System started, pulling last saved URL queue.");
                }
                ois.close();
                fis.close();
            } catch (IOException e) {
                System.out.println("Error trying to read \"QUEUE.obj\".");
            } catch (ClassNotFoundException e) {
                System.out.println("Class \"QUEUE\" not found.");
            }
        } else {
            System.out.println("Queue: Queue started empty, there was no data to read.");
        }
    }

}
