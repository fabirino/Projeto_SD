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
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.StringTokenizer;

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

    public Downloader() throws RemoteException {
        super();
    }

    public static void main(String[] args) {

        PTStopWords = new ArrayList<>();
        ENStopWords = new ArrayList<>();
        fillArray(PTStopWords, PT);
        fillArray(ENStopWords, EN);

        Downloader downloader;
        try {
            downloader = new Downloader();
            MulticastSocket socket = null;
            InetAddress group;

            // Catch Crtl C to save data
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    // Esperar que o Downloader processe o url
                    System.out.println("Downloader: Shutdown");
                    try {
                        SMi.unsubscribeD((DownloaderInterfaceC) downloader);
                    } catch (RemoteException re) {
                        re.printStackTrace();
                    }
                }
            });

            try {
                SMi = (DownloaderInterface) Naming.lookup("rmi://localhost:1099/SM");
                boolean exit = SMi.subscribeD((DownloaderInterfaceC) downloader);
                System.out.println(exit);
                if (exit == false) {
                    System.out.println("Downloader: There are no Storage Barrels available");
                    System.exit(2);
                }
                socket = new MulticastSocket(PORT); // create socket and bind it
                group = InetAddress.getByName(MULTICAST_ADDRESS);
                socket.joinGroup(group);

                // TODO: substituir os returns por algo sustentavel
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
            while (true) {
                try {
                    // Pega o ultimo URL da Fila e faz o crawl
                    URL url = SMi.getURLQueue();
                    System.out.println("Downloader: Indexing " + url);
                    url = downloader.crawlURL(url, SMi);

                    // Envia o URL por multicast para os Barrels
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(url);
                    byte[] data = baos.toByteArray();

                    socket.send(new DatagramPacket(data, data.length, group, PORT));

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
        } catch (RemoteException e) {
            System.out.println("Downloader: Somthing went wrong :)");
        }
    }

    /**
     * O URL quando chega ao Downloader apenas tem o link, e necessario depois
     * adicionar a quote, title, keywords e lista de URL's
     * 
     * @param url object recived to crawl and fill the data
     * @param SMi Search Module interface
     * @return URL object
     */
    public URL crawlURL(URL url, DownloaderInterface SMi) {
        // TODO: Nao colocar os url todos "mamados", ou seja, javascript e cenas assim que esta a guardar isso na class URL

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
                if (!(ENStopWords.contains(strLower) || PTStopWords.contains(strLower)))
                    url.addKeyword(str);
                // Quote
                if (countTokens++ < 20) {
                    quote += (str + " ");
                }
            }
            quote += "...";

            // Other URLs inside the first URL
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                System.out.println(link.attr("abs:href"));
                url.addURL(link.attr("abs:href"));
                SMi.addURLQueue(new URL(link.attr("abs:href")));
            }
        } catch (MalformedURLException MFE) {
            System.out.println("Downloader: The URL specified is malformed");
        } catch (IOException e) {
            e.printStackTrace();
        }

        url.setTitle(title);
        url.setQuote(quote);

        return url;
    }

    public static void fillArray(ArrayList<String> AL, String[] list) {
        for (int i = 0; i < list.length; i++) {
            AL.add(list[i]);
        }
    }
}
