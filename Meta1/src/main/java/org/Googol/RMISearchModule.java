package org.Googol;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import io.github.cdimascio.dotenv.Dotenv;

import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;

/**
 * <p>
 * Porta de entrada para o sistema
 * <p>
 * O Search Module escolhe um Storage Barrel para responder a cada pesquisa
 * <p>
 * Comunica com o Storage Barrels por RMI
 */
public class RMISearchModule extends UnicastRemoteObject
        implements GoogolInterface, StorageBarrelInterface, DownloaderInterface {
    static ArrayList<StorageBarrelInterfaceB> listOfBarrels;
    static int barrelCount;
    static ArrayList<DownloaderInterfaceC> listOfDownloaders;
    static int downloaderCount;
    static Connection connection;
    static boolean sync;

    String menu;
    Queue urlQueue;
    Queue urlindexQueue;
    int nextBarrel = 0;

    /**
     * Constructor
     * @param i Distinguishes the Interface used
     * @throws RemoteException
     */
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
            urlQueue = new Queue("_crawl");
            urlindexQueue = new Queue("_index");
            listOfBarrels = new ArrayList<StorageBarrelInterfaceB>();
            listOfDownloaders = new ArrayList<DownloaderInterfaceC>();
        }
    }

    public static void main(String[] args) throws RemoteException {
        Dotenv dotenv = Dotenv.load();
        downloaderCount = 0;
        barrelCount = 0;
        sync = false;

        // Setup DataBase
        String url = "jdbc:postgresql://localhost/ProjetoSD";
        String username = dotenv.get("DB_USER");
        String password =  dotenv.get("DB_PASSWORD");
        try {
            DriverManager.registerDriver(new org.postgresql.Driver());
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Search Module: Connected to Database");
        } catch (SQLException e) {
            System.out.println("Search Module: Error connecting to DataBase");
        }

        GoogolInterface SMi = new RMISearchModule(0);
        StorageBarrelInterface SMi2 = new RMISearchModule(1);

        System.out.println("Search Module: Server ready");
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
            // LocateRegistry.createRegistry(1099).rebind("rmi://<public-ip>:1099/SM", SMi);// DEBUG: out off machine
        } catch (RemoteException RE) {
            System.out.println("Search Module: System crashed, Remote Exception ocurred");
            SMi.queueCrash();
        } finally {

        }

        try {
            LocateRegistry.createRegistry(1098).rebind("SB", SMi2);
            // LocateRegistry.createRegistry(1098).rebind("rmi://<public-ip>:1098/SB", SMi2);// DEBUG: out off machine
        } catch (Exception re) {
            System.out.println("Exception in Search Module: " + re);
        }
    }

    // #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=
    // Googol Interface functions
    // #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=

    public void newURL(String URLString) throws RemoteException {
        System.out.println("Search Module: Adding \"" + URLString + "\" to the QUEUE");
        urlQueue.addURLHead(new URL(URLString));
    }


    public Response pagesWithWord(String[] words, int pages) throws RemoteException {
        if (listOfBarrels.size() == 0) {
            Response response = new Response("\nThere are no active barrels!", 0);
            return response;
        }

        // Add the search to the topSearches
        if (pages == 0) {
            for (String word : words) {
                addSearchDB(word);
            }
        }

        // Choose a barrel to work (circular)
        StorageBarrelInterfaceB Barrel = listOfBarrels.get((nextBarrel++) % listOfBarrels.size());
        Response result = Barrel.getUrlsToClient(words, pages);


        if (result != null) {
            return result;
        } else if (result == null && pages > 0) {
            Response response = new Response("\nThere are no more Urls with that word!", 0);
            System.out.println("Search Module: There are no more Urls with that word!");
            return response;
        } else {
            String text = "\nThere are no Urls with that word!";
            Response response = new Response(text, 0);
            System.out.println(response.getText());
            System.out.println("Search Module: There are no Urls with that word!");
            return response;
        }

    }


    public Response pagesWithURL(String URL, int pages) throws RemoteException {
        if (listOfBarrels.size() == 0) {
            Response response = new Response("\nThere are no active barrels!", 0);
            return response;
        }

        // Choose a barrel to work (circular)
        StorageBarrelInterfaceB Barrel = listOfBarrels.get((nextBarrel++) % listOfBarrels.size());
        

        Response result = Barrel.getpagesWithURL(URL, pages);
        if (result != null) {
            return result;
        } else if (result == null && pages > 0) {
            Response response = new Response("\nThere are no more Urls with that URL!", 0);
            return response;
        } else {
            Response response = new Response("\nThere are no Urls with that URL!", 0);
            return response;
        }
    }

    public String adminPage() throws RemoteException, SQLException {
        String result = "Active Barrels: " + listOfBarrels.size() + "\n";
        int count = 0;
        for (StorageBarrelInterfaceB barrel : listOfBarrels) {
            result += "Barrel" + barrel.getId() + "\n";
        }
        result += "Active Downloaders: " + listOfDownloaders.size() + "\n";
        count = 0;
        for (DownloaderInterfaceC downloader : listOfDownloaders) {
            result += "Downloader" + downloader.getId() + "\n";
        }

        result += "Most commun Searches (Word-> Number of Searches):\n";
        count = 0;

        String check = "SELECT word, num FROM topSearches ORDER BY num DESC";
        PreparedStatement checkStatement = connection.prepareStatement(check);
        ResultSet rs = checkStatement.executeQuery();
        String word = "";
        int num = 0;

        while (rs.next()) {
            word = rs.getString("word");
            num = rs.getInt("num");
            result += word + "-> " + num + "\n";

            if (++count == 10)
                break;
        }

        return result;
    }

    public String menu() throws RemoteException {
        return menu;
    }

    public int login(String username, String password) throws RemoteException, SQLException {
        // check if the username exists
        String query = "SELECT username FROM users WHERE username LIKE ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, username);
        ResultSet rs = statement.executeQuery();

        if (rs.next()) {
            // user exists
            query = "SELECT password FROM users WHERE username LIKE ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, username);
            rs = statement.executeQuery();

            if (rs.next()) {
                if (password.equals(rs.getString("password"))) {
                    // the password matches
                    return 1;
                } else {
                    // wrong password
                    return 0;
                }
            }
        } else {
            // the username doesnt exists
            return 2;
        }

        return 0;
    }

    public int register(String username, String password) throws RemoteException, SQLException {
        // Check if there is another user with the same usename
        String query = "SELECT username FROM users WHERE username LIKE ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, username);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            // user already exists
            return 0;
        } else {
            try {
                // Encrypt password
                String encrypted = null;
                MessageDigest m = MessageDigest.getInstance("MD5");
                m.update(password.getBytes());
                byte[] bytes = m.digest();
                StringBuilder s = new StringBuilder();
                for (int i = 0; i < bytes.length; i++) {
                    s.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
                }
                encrypted = s.toString();

                // Save to DB
                query = "INSERT INTO users (username, password) values(?,?)";
                PreparedStatement statement2 = connection.prepareStatement(query);
                statement2.setString(1, username);
                statement2.setString(2, encrypted);
                statement2.executeUpdate();
            } catch (NoSuchAlgorithmException e) {
                System.out.println("Search Module: Error encrypting password on login, no such encrypting algorithm");
            }
            return 1;
        }

    }


    // #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=
    // Storage Barrel Interface functions
    // #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=

    public int subscribeB(String name, StorageBarrelInterfaceB c) throws RemoteException {
        System.out.println("Search Module: Subscribing Barrel" + ++barrelCount);
        listOfBarrels.add(c);
        for (DownloaderInterfaceC cl : listOfDownloaders) {
            cl.setvariavel(listOfBarrels.size());
            cl.setsyncD(true);
        }
        return barrelCount;
    }

    public void unsubscribeB(StorageBarrelInterfaceB client) throws RemoteException {
        try {
            listOfBarrels.remove(client);
        } catch (Exception e) {
            System.out.println("ARDEU A TENDA!");
        }
        for (DownloaderInterfaceC cl : listOfDownloaders) {
            cl.setvariavel(listOfBarrels.size());
        }
        System.out.println("Search Module: Unsubscribing Barrel" + client.getId());
    }

    public HashMap<String, HashSet<URL>> syncIndex(StorageBarrelInterfaceB c ,HashMap<String, HashSet<URL>> index) throws RemoteException{
        HashMap<String, HashSet<URL>> hash;

        if(listOfBarrels.size() == 1){
            return null;
        }else{
            if(index.size() == 0){
                StorageBarrelInterfaceB Barrel = listOfBarrels.get((nextBarrel++) % listOfBarrels.size());
                hash = Barrel.getIndex();
                return hash;
            }
            else{
                StorageBarrelInterfaceB Barrel = listOfBarrels.get((nextBarrel++) % listOfBarrels.size());
                hash = Barrel.getIndex();
                for (String key : index.keySet()) {
                    if (hash.containsKey(key)) {
                        HashSet<URL> set1 = hash.get(key);
                        HashSet<URL> set2 = index.get(key);
                        for (URL s : set2) {
                            boolean contains = false;
                            String currentUrl = s.getUrl();
                            for (URL url : set1) {
                                if (currentUrl.equals(url.getUrl())) {
                                    contains = true;
                                    break;
                                }
                            }
                            if (!contains) {
                                set1.add(s);
                            }
                        }
        
                        System.out.println(set1);
                    } else {
                        hash.put(key, index.get(key));
                    }
                }
                for (StorageBarrelInterfaceB b : listOfBarrels) {
                    b.setIndex(hash);
                }
                return null;
            }
        }

    }

    public HashMap<String, HashSet<URL>> syncPath(StorageBarrelInterfaceB c ,HashMap<String, HashSet<URL>> path) throws RemoteException{
        HashMap<String, HashSet<URL>> hash;

        if(listOfBarrels.size() == 1){
            return null;
        }else{
            StorageBarrelInterfaceB Barrel = listOfBarrels.get((nextBarrel++) % listOfBarrels.size());
            if(path.size() == 0){
                hash = Barrel.getPath();
                return hash;
            }
            else{
                hash = Barrel.getPath();
                for (String key : path.keySet()) {
                    if (hash.containsKey(key)) {
                        HashSet<URL> set1 = hash.get(key);
                        HashSet<URL> set2 = path.get(key);
                        for (URL s : set2) {
                            boolean contains = false;
                            String currentUrl = s.getUrl();
                            for (URL url : set1) {
                                if (currentUrl.equals(url.getUrl())) {
                                    contains = true;
                                    break;
                                }
                            }
                            if (!contains) {
                                set1.add(s);
                            }
                        }
        
                    } else {
                        hash.put(key, path.get(key));
                    }
                }
                for (StorageBarrelInterfaceB b : listOfBarrels) {
                    b.setPath(hash);
                }
                return null;
            }
        }

    }

    public void updatesyncD() throws RemoteException {
        for (DownloaderInterfaceC cl : listOfDownloaders) {
            cl.setsyncD(false);
        }
        
    }

    // #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=
    // Downloader Interface functions
    // #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=

    public int subscribeD(DownloaderInterfaceC c) throws RemoteException {
        listOfDownloaders.add(c);
        if (listOfBarrels.size() > 0) {
            System.out.println("Search Module: Subscribing Downloader" + ++downloaderCount);
            return downloaderCount;
        } else {
            System.out.println("Search Module: Subscribing Downloader" + downloaderCount);
            return 0;
        }
    }

    public void unsubscribeD(DownloaderInterfaceC client) throws RemoteException {
        try {
            listOfDownloaders.remove(client);
        } catch (Exception e) {
            System.out.println("ARDEU A TENDA!");
        }
        System.out.println("Search Module: Unsubscribing Downloader" + client.getId());
    }

    public int getNBarrels() throws RemoteException{
        return listOfBarrels.size();
    }

    public void pingBarrels() throws RemoteException{
        int count=0;
        System.out.println("Search Module: A packet was lost, pinging all Barrels");
        for(StorageBarrelInterfaceB barrel: listOfBarrels){
            try{
                if(barrel.tryPing()){
                    count++;
                    // System.out.println(count);
                    // System.out.println("Barrel " + barrel.getId() + " is alive");
                }
            } catch (RemoteException e){
                System.out.println("Search Module: A barrel stopped responding. Removing from list of active Barrels");
                listOfBarrels.remove(barrel);
                System.out.println("Search Module: Unsubscribing this Barrel");
                for (DownloaderInterfaceC cl : listOfDownloaders) {
                    cl.setvariavel(listOfBarrels.size());
                }
                break;
            } 
        }
        // System.out.println("Saiu do loop");
    }

    // #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=
    // QUEUE functions
    // #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=

    //URLQUEUE
    public boolean addURLQueue(URL URL) throws RemoteException {
        return urlQueue.addURL(URL);
    }
    
    public URL getURLQueue() throws RemoteException, InterruptedException {
        return urlQueue.getUrl();
    }
    
    public void queueRecovery() throws RemoteException {
        urlQueue.onRecovery();
        urlindexQueue.onRecovery();
    }
    
    public void queueCrash() throws RemoteException {
        urlQueue.onCrash();
        urlindexQueue.onCrash();
    }

    //URLINDEXQUEUE
    public boolean addURLQueue2(URL URL) throws RemoteException {
        // System.out.println("Search Module: Adding \"" + URL.getUrl() + "\" to the QUEUE2");
        return urlindexQueue.addURL(URL);
    }

    public boolean checkUrlQueue2(URL URL) throws RemoteException {
        return urlindexQueue.checkUrl(URL);
    }

    // #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=
    // DataBase TODO: ver problemas de concorrencia
    // #=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=#=

    /**
     * 
     * @param word word to add to the Top Searches
     */
    public void addSearchDB(String word) {
        try {
            String check = "SELECT num FROM topSearches WHERE word = ?";
            PreparedStatement checkStatement = connection.prepareStatement(check);
            checkStatement.setString(1, word);
            ResultSet rs = checkStatement.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("num");
                String query = "UPDATE topSearches SET num = ? WHERE word = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, count + 1);
                statement.setString(2, word);
                statement.executeUpdate();

            } else {
                String query = "INSERT INTO topSearches (word, num) values(?,?)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, word);
                statement.setInt(2, 1);
                statement.executeUpdate();
            }

        } catch (SQLException e) {
            System.out.println("Search Module: Error trying so read/write to DataBase");
            e.printStackTrace();
        }

    }

}
