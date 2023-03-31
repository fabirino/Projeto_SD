package org.Googol;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.helper.ValidationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPOutputStream;

/**
 * <p>
 * Trabalham em paralelo
 * <p>
 * Usam uma QUEUE de URLs para processar a informacao
 * <p>
 * Obtem a informacao das paginas Web com a ajuda da biblioteca Jsoup
 * <p>
 * Enviam essa informacao por multicast aos Storage Barels
 * <p>
 * 1 URL indexado por 1 Downloader
 */
public class Downloader extends UnicastRemoteObject implements DownloaderInterfaceC {

    private static final String MULTICAST_ADDRESS = "224.3.2.1";
    private static final int PORT = 4321;
    private static int PORTUDP = 6789;
    private static String[] PT = { "de ", "a ", "o ", "que ", "e ", "do ", "da ", "em ", "um ", "para ", "é ", "com ",
            "não ", "uma ", "os ", "no ", "se ", "na ", "por ", "mais ", "as ", "dos ", "como ", "mas ", "foi ", "ao ",
            "ele ", "das ", "tem ", "à ", "seu ", "sua ", "ou ", "ser ", "quando ", "muito ", "há ", "nos ", "já ",
            "está ", "eu ", "também ", "só ", "pelo ", "pela ", "até ", "isso ", "ela ", "entre ", "era ", "depois ",
            "sem ", "mesmo ", "aos ", "ter ", "seus ", "quem ", "nas ", "me ", "esse ", "eles ", "estão ", "você ",
            "tinha ", "foram ", "essa ", "num ", "nem ", "suas ", "meu ", "às ", "minha ", "têm ", "numa ", "pelos ",
            "elas ", "havia ", "seja ", "qual ", "será ", "nós ", "tenho ", "lhe ", "deles ", "essas ", "esses ",
            "pelas ", "este ", "fosse ", "dele ", "tu ", "te ", "vocês ", "vos ", "lhes ", "meus ", "minhas", "teu ",
            "tua", "teus", "tuas", "nosso ", "nossa", "nossos", "nossas", "dela ", "delas ", "esta ", "estes ",
            "estas ", "aquele ", "aquela ", "aqueles ", "aquelas ", "isto ", "aquilo ", "estou", "está", "estamos",
            "estão", "estive", "esteve", "estivemos", "estiveram", "estava", "estávamos", "estavam", "estivera",
            "estivéramos", "esteja", "estejamos", "estejam", "estivesse", "estivéssemos", "estivessem", "estiver",
            "estivermos", "estiverem", "hei", "há", "havemos", "hão", "houve", "houvemos", "houveram", "houvera",
            "houvéramos", "haja", "hajamos", "hajam", "houvesse", "houvéssemos", "houvessem", "houver", "houvermos",
            "houverem", "houverei", "houverá", "houveremos", "houverão", "houveria", "houveríamos", "houveriam", "sou",
            "somos", "são", "era", "éramos", "eram", "fui", "foi", "fomos", "foram", "fora", "fôramos", "seja",
            "sejamos", "sejam", "fosse", "fôssemos", "fossem", "for", "formos", "forem", "serei", "será", "seremos",
            "serão", "seria", "seríamos", "seriam", "tenho", "tem", "temos", "tém", "tinha", "tínhamos", "tinham",
            "tive", "teve", "tivemos", "tiveram", "tivera", "tivéramos", "tenha", "tenhamos", "tenham", "tivesse",
            "tivéssemos", "tivessem", "tiver", "tivermos", "tiverem", "terei", "terá", "teremos", "terão", "teria",
            "teríamos", "teria" };

    private static String[] EN = { "i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours",
            "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers", "herself", "it", "its",
            "itself", "they", "them", "their", "theirs", "themselves", "what", "which", "who", "whom", "this", "that",
            "these", "those", "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "having",
            "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as", "until", "while",
            "of", "at", "by", "for", "with", "about", "against", "between", "into", "through", "during", "before",
            "after", "above", "below", "to", "from", "up", "down", "in", "out", "on", "off", "over", "under", "again",
            "further", "then", "once", "here", "there", "when", "where", "why", "how", "all", "any", "both", "each",
            "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so", "than",
            "too", "very", "s", "t", "can", "will", "just", "don", "should", "no" };

    private static ArrayList<String> PTStopWords;
    private static ArrayList<String> ENStopWords;
    private static DownloaderInterface SMi;
    private static MulticastSocket socket;
    private static Downloader downloader;
    private static int id;
    private static Lock lock = new ReentrantLock();
    private final static Condition condicao = lock.newCondition();
    private static int variavel = 1;
    private static boolean sync;
    public static String ipServer;

    /**
     * Construtor
     */
    public Downloader() throws RemoteException {
        super();
    }

    /**
     * Number or active barrels, if it is 0, waits
     * @throws InterruptedException
     */
    public static void esperarVariavel() throws InterruptedException {
        lock.lock();
        try {
            while (variavel == 0 || sync == true) {
                System.out.println("Downloader: Waiting for barrels to remain active!");
                condicao.await();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * signals the wait
     * @param valor value to change
     */
    public void mudarVariavelint(int valor) {
        lock.lock();
        try {
            variavel = valor;
            condicao.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * signals the wait
     * @param valor value to change
     */
    public void mudarVariavelsync(Boolean valor) {
        lock.lock();
        try {
            sync = valor;
            condicao.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {

        if (args.length == 1) {
            ipServer = args[0];
        } else {
            System.out.println("Barrel: Use the ip address of the server as an arg");
            return;
        }

        PTStopWords = new ArrayList<>();
        ENStopWords = new ArrayList<>();
        fillArray(PTStopWords, PT);
        fillArray(ENStopWords, EN);

        try {
            downloader = new Downloader();
            socket = null;
            InetAddress group;
            sync = false;

            // Catch Crtl C to save data
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    // Esperar que o Downloader processe o url
                    System.out.println("Downloader: Shutdown");
                    try {
                        SMi.unsubscribeD((DownloaderInterfaceC) downloader);
                    } catch (RemoteException re) {
                        // re.printStackTrace();
                        System.out.println("Downloader: The Search Module is no longer running");
                    }
                }
            });

            try {
                SMi = (DownloaderInterface) Naming.lookup("rmi://"+ipServer+":1099/SM");
                int num = SMi.subscribeD((DownloaderInterfaceC) downloader);
                if (num == 0) {
                    System.out.println("Downloader: There are no Storage Barrels available");
                    System.exit(2);
                }
                downloader.setId(num);
                PORTUDP += id;
                System.out.println("PORTUDP-> " + PORTUDP);
                socket = new MulticastSocket(PORT); // create socket and bind it
                group = InetAddress.getByName(MULTICAST_ADDRESS);
                socket.joinGroup(group);

            } catch (NotBoundException NBE) {
                System.out.println("Downloader: The interface is not bound");
                return;
            } catch (RemoteException RM) {
                System.out.println("Downloader: Remote Exception catched, Search Module might not be running");
                return;
            } catch (IOException IO) {
                System.out.println("Downloader: Could not join Multicast group");
                return;
            }

            System.out.println("Downloader: System started");
            try (DatagramSocket aSocket = new DatagramSocket(PORTUDP)) {
                aSocket.setSoTimeout(1000);// timeout de 1 segundo!!
                while (true) {
                    try {
                        esperarVariavel();

                        // Pega o ultimo URL da Fila e faz o crawl
                        URL url = SMi.getURLQueue();
                        System.out.println("Downloader: Indexing " + url);
                        url = downloader.crawlURL(url, SMi);
                        if (url == null)
                            continue;

                        Message m = new Message(url, PORTUDP);
                        int attempts = 3; // DEBUG: 3 tentativas se o multicast enviar e algo falhar!!
                        for (int i = 0; i < attempts; i++) {
                            try {
                                // Envia o URL por multicast para os Barrels
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(baos));
                                oos.writeObject(m);
                                oos.close();
                                byte[] data = baos.toByteArray();

                                // System.out.println("lenght -->>" + data.length);
                                socket.send(new DatagramPacket(data, data.length, group, PORT));

                                // Esperar pelo akn
                                // System.out.println("========udp!!==========");
                                int nbarrel = SMi.getNBarrels();
                                if (nbarrel == 0) {
                                    SMi.addURLQueue(url);
                                    System.out.println("Send to QUEUE again!");
                                    break;
                                }
                                // System.out.println("n barrels -> " + nbarrel);
                                for (int j = 0; j < nbarrel; j++) {
                                    byte[] buffer = new byte[1000];
                                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);

                                    aSocket.receive(request);

                                    String s = new String(request.getData(), 0, request.getLength());
                                    System.out.println("Server Recebeu: " + s + " de: "
                                            + request.getAddress()
                                            + " no porto "
                                            + request.getPort());

                                }
                            } catch (SocketTimeoutException e) {
                                // timeout exception.
                                System.out.println("Timeout reached!!! " + e);
                                SMi.pingBarrels();
                                if (i == 2) {
                                    SMi.addURLQueue(url);
                                    System.out.println("Send to QUEUE again!");
                                } else {
                                    continue;
                                }
                            }
                            break;

                        }

                    } catch (RemoteException RE) {
                        System.out.println("Downloader: Remote Exception catched");
                    } catch (InterruptedException e) {
                        System.out.println(e);
                    } catch (IOException IO) {
                        System.out.println("Downloader: Failed to send data through Multicast");
                    } catch (ValidationException Ve) {
                        System.out.println("Downloader: Validation Exception catched");
                    }
                }
            } catch (SocketException e) {
                System.out.println("Socket: " + e.getMessage());
            }
        } catch (RemoteException e) {
            System.out.println("Downloader: Something went wrong :)");
        }
    }

    /**
     * The URL received by the Downloader only has the link. It is necessary
     * to add its title, quote, keywords and list of UELs
     * 
     * @param url object recived to crawl and fill the data
     * @param SMi Search Module interface
     * @return URL object
     */
    public URL crawlURL(URL url, DownloaderInterface SMi) {

        // try catch para apanhar strings que nao sejam URLs
        String urlString = url.getUrl();
        String title = "";
        String quote = "";
        String str = "";

        try {
            Document doc = Jsoup.connect(urlString).get();
            title = doc.title();

            // Keywords
            StringTokenizer tokens = new StringTokenizer(doc.text());
            int countTokens = 0;
            while (tokens.hasMoreElements()) {
                str = tokens.nextToken();
                String strLower = str.toLowerCase();
                if (!(ENStopWords.contains(strLower) || PTStopWords.contains(strLower))) {
                    url.addKeyword(strLower);
                }
                // Quote
                if (countTokens++ < 20) {
                    quote += (str + " ");
                }
            }
            quote += "...";

            // Other URLs inside the first URL
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                // System.out.println(link.attr("abs:href"));
                url.addURL(link.attr("abs:href"));
                SMi.addURLQueue(new URL(link.attr("abs:href")));
            }
        } catch (MalformedURLException MFE) {
            System.out.println("Downloader: The URL specified is malformed");
            return null;
        } catch (IllegalArgumentException IAE) {
            System.out.println("Downloader: Unable to crawl " + url.getUrl());
            return null;
        } catch (RemoteException e) {
            System.out.println("Downloader: The Search Module is no longer running");
            // e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Downloader: Unable to crawl " + url.getUrl());
        }

        url.setTitle(title);
        url.setQuote(quote);

        return url;
    }

    /**
     * Used to convert the StopWords in an ArrayList
     * 
     * @param AL   Arraylist to store the keywords
     * @param list List of keywords
     */
    public static void fillArray(ArrayList<String> AL, String[] list) {
        for (int i = 0; i < list.length; i++) {
            AL.add(list[i]);
        }
    }

    public void crashSearchModel() throws RemoteException {
        System.out.println("Downloader: Shutdown");
        try {
            SMi.unsubscribeD((DownloaderInterfaceC) downloader);
        } catch (RemoteException re) {
            System.out.println("Downloader: The Search Module is no longer running");
            // re.printStackTrace();
        }
        System.exit(0);
    }

    public int getId() throws RemoteException {
        return id;
    }

    /**
     * Set Method
     * @param id2 param to set
     */
    public void setId(int id2) {
        id = id2;
    }

    /** 
     * Used by the server when a barrels is active or a barrel dies
     * @param variavel number of barrels
     * @throws RemoteException
     */
    public void setvariavel(int variavel) throws RemoteException {
        mudarVariavelint(variavel);
    }

    /**
     * used to sync the information between barrels
     * @param variavel change to true, and the barrels wait
     * @throws RemoteException
     */
    public void setsyncD(Boolean variavel) throws RemoteException {
        mudarVariavelsync(variavel);
    }
}
