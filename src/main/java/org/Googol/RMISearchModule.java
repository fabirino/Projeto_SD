package org.Googol;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Vector;

/**
 * <p>
 * Porta de entrada para o sistema
 * <p>
 * O Search Module escolhe um Storage Barrel para responder a cada pesquisa
 * <p>
 * Comunica com o Storage Barrels por RMI
 */
// TODO: SUBSTITUIR ARRAYLIST POR QQ CENA THREAD SAFE , USEI O VECTOR MAS O STOR
// TINHA DITO QUE JA ESTAVA ULTRAPASSADO SLA
public class RMISearchModule extends UnicastRemoteObject
        implements GoogolInterface, StorageBarrelInterface, DownloaderInterface {
    static ArrayList<StorageBarrelInterfaceB> listOfBarrels;
    static ArrayList<DownloaderInterfaceC> listOfDownloaders;
    static TreeMap<String, Integer> topSearches;
    private File searchFile;
    String menu;
    Queue urlQueue;
    int nextBarrel = 0;

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
            listOfDownloaders = new ArrayList<DownloaderInterfaceC>();
            topSearches = new TreeMap<String, Integer>();
            searchFile = new File("./info\\TOPSEARCH.obj");
        }
    }

    public static void main(String[] args) throws RemoteException {
        
        System.out.println("Search Module: Server ready");
        GoogolInterface SMi = new RMISearchModule(0);
        StorageBarrelInterface SMi2 = new RMISearchModule(1);

        // Catch Crtl C to save data
        Thread t0 = new Thread("t0") {
            public void run() {
                try {
                    SMi.queueCrash();
                } catch (RemoteException re) {
                    re.printStackTrace();
                } finally {
                    System.out.println("Search Module: Shutdown");
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(t0);

        try {
            LocateRegistry.createRegistry(1099).rebind("SM", SMi);
        } catch (RemoteException RE) {
            System.out.println("Search Module: System crashed, Remote Exception ocurred");
            // FIXME: nao sei se o crash e aqui ou no finally
            SMi.queueCrash();
        } finally {

        }

        try {
            LocateRegistry.createRegistry(1098).rebind("SB", SMi2);
        } catch (Exception re) {
            System.out.println("Exception in Search Module: " + re);
        }
    }

    // #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=
    // Googol Interface functions
    // #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=

    public void newURL(String URLString) throws RemoteException {
        System.out.println("Search Module: Adding \"" + URLString + "\" to the QUEUE");
        urlQueue.addURL(new URL(URLString));
    }

    public String pagesWithWord(String[] words, int pages) throws RemoteException {
        // public void pagesWithWord(String[] word) throws RemoteException {
        String ret = "";
        if (listOfBarrels.size() == 0) {
            return "\nThere are no active barrels!";
        }

        // Add the search to the topSearches
        for (String word : words) {
            addSearch(word);
        }

        // Choose a barrel to work (circular)
        StorageBarrelInterfaceB Barrel = listOfBarrels.get((nextBarrel++) % listOfBarrels.size());
        HashSet<URL> hash = Barrel.getUrlsToClient(words, pages);
        ret = "\n";
        if (hash != null) {
            for (URL url : hash) {
                ret += url.toString() + '\n';
            }
            return ret;
        } else if (hash == null && pages != 1) {
            return "\nThere are no more Urls with that word!";
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

        // Choose a barrel to work (circular)
        StorageBarrelInterfaceB Barrel = listOfBarrels.get((nextBarrel++) % listOfBarrels.size());
        HashSet<String> hash = Barrel.getpagesWithURL(URL, 0);
        if (hash != null) {
            for (String url : hash) {
                a.add(url);
            }
            return a;
        } else {
            a.add("There are no Urls with that URL!");
            return a;
        }
    }

    public String adminPage() throws RemoteException {
        String result = "Active Barrels: " + listOfBarrels.size() + "\n";
        int count = 0;
        for (StorageBarrelInterfaceB barrel : listOfBarrels) {
            result += "Barrel " + ++count + "\n";
        }
        result += "Active Downloaders: " + listOfDownloaders.size() + "\n";
        count = 0;
        for (DownloaderInterfaceC downloader : listOfDownloaders) {
            result += "Downloader " + ++count + "\n";
        }

        result += "Most commun Searches:\n";
        count = 0;

        TreeMap<String, Integer> reverseTreeMap = valueSort(topSearches);

        if (reverseTreeMap.size() > 10) {
            for (String key : reverseTreeMap.descendingKeySet()) {
                result += key + ": " + topSearches.get(key) + "x\n";
                if (count++ == 10) {
                    break;
                }
            }
        } else {
            for (String key : reverseTreeMap.descendingKeySet()) {
                result += key + ": " + topSearches.get(key) + "x\n";
            }
        }

        return result;
    }

    public String menu() throws RemoteException {
        return menu;
    }

    // #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=
    // Storage Barrel Interface functions
    // #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=

    public void subscribe(String name, StorageBarrelInterfaceB c) throws RemoteException {
        System.out.println("Search Module: Subscribing barrel" + listOfBarrels.size());
        listOfBarrels.add(c);
    }

    public void unsubsribe(StorageBarrelInterfaceB client) throws RemoteException {
        try {
            listOfBarrels.remove(client);
        } catch (Exception e) {
            System.out.println("ARDEU A TENDA!");
        }
        System.out.println("Search Module: Unsubscribing barrel" + listOfBarrels.size());
    }

    // #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=
    // Downloader Interface functions
    // #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=

    public boolean subscribeRMI(DownloaderInterfaceC c) throws RemoteException {
        System.out.println("Search Module: Subscribing Downloaders" + listOfDownloaders.size());
        listOfDownloaders.add(c);
        if (listOfBarrels.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void unsubsribeRMI(DownloaderInterfaceC client) throws RemoteException {
        try {
            listOfDownloaders.remove(client);
        } catch (Exception e) {
            System.out.println("ARDEU A TENDA!");
        }
        System.out.println("Search Module: Unsubscribing Downloader" + listOfDownloaders.size());
    }

    // #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=
    // QUEUE functions
    // #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=

    public boolean addURLQueue(URL URL) throws RemoteException {
        return urlQueue.addURL(URL);
    }

    public URL getURLQueue() throws RemoteException, InterruptedException {
        return urlQueue.getUrl();
    }

    public void queueRecovery() throws RemoteException {
        urlQueue.onRecovery();
    }

    public void queueCrash() throws RemoteException {
        urlQueue.onCrash();
    }

    // #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=
    // OTHERS
    // #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=

    public boolean addSearch(String word) {
        if (topSearches.containsKey(word)) {
            int old = topSearches.get(word);
            return topSearches.replace(word, old, old + 1);
        } else {
            topSearches.put(word, 1);
        }
        return true;
    }

    /**
     * Copied from
     * https://www.geeksforgeeks.org/how-to-sort-a-treemap-by-value-in-java/
     * 
     * @param <K> key
     * @param <V> value
     * @param map map to be sorted
     * @return map sorted by reverse order of values
     */
    public static <K, V extends Comparable<V>> TreeMap<K, V> valueSort(final TreeMap<K, V> map) {
        // Static Method with return type Map and
        // extending comparator class which compares values
        // associated with two keys
        Comparator<K> valueComparator = new Comparator<K>() {

            public int compare(K k1, K k2) {

                int comp = map.get(k1).compareTo(map.get(k2));

                if (comp == 0)
                    return 1;

                else
                    return comp;
            }
        };

        // SortedMap created using the comparator
        TreeMap<K, V> sorted = new TreeMap<K, V>(valueComparator);
        sorted.putAll(map);
        return sorted;
    }

}
