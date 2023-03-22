package org.Googol;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Trabalham em paralelo
 * Usam uma QUEUE de URLs para processar a informacao
 * Obtem a informacao das paginas Web com a ajuda da biblioteca Jsoup
 * Enviam essa informacao por multicast aos Storage Barels
 * 1 URL indexado por 1 Downloader
 *
 */
public class Downloader{

    public static void main(String[] args) {
        GoogolInterface SMi;
        Downloader downloader = new Downloader();

        try {
            SMi = (GoogolInterface) Naming.lookup("rmi://localhost/SM");
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

        while (true) {
            try {
                String url = SMi.getURLQueue().getUrl();
                System.out.println("Indexing " + url);
                downloader.crawlURL(url, SMi);
            } catch (RemoteException RE) {
                System.out.println("System: Remote Exception catched");
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }

    public URL crawlURL(String url, GoogolInterface SMi) {

        // try catch para apanhar strings que nao sejam URLs
        String title = "";
        String quote = "";
        String str = "";
        List<String> keywords = new ArrayList<>();
        List<String> urls = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(url).get();
            title = doc.title();

            // Keywords
            StringTokenizer tokens = new StringTokenizer(doc.text());
            int countTokens = 0;
            while (tokens.hasMoreElements()) {
                System.out.println(tokens.nextToken().toLowerCase());
                str = tokens.nextToken();
                keywords.add(str.toLowerCase());
                // Quote
                if (countTokens++ < 20) {
                    quote += (str + " ");
                }
            }
            quote += "...";
            // Other URLs inside the first URL
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                System.out.println(link.text() + "\n" + link.attr("abs:href") + "\n");
                urls.add(link.text());
                // TODO: catch some exception
                SMi.addURLQueue(new URL(link.attr("abs:href")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new URL(url, title, quote, keywords, urls);
    }
}

