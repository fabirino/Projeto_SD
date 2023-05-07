package org.Googol;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.Googol.forms.URL_forms;
import org.Googol.forms.User;
import org.Googol.forms.Words;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ConcurrentModificationException;

import org.springframework.ui.Model;

@Controller
public class Controller1 {

    GoogolInterface SMi;

    public Controller1() {
        try {
            this.SMi = (GoogolInterface) Naming.lookup("rmi://localhost:1099/SM");
        } catch (NotBoundException NBE) {
            System.out.println("System: The Interface is not bound");
            return;
        } catch (MalformedURLException MFE) {
            System.out.println("System: The URL specified is malformed");
            return;
        } catch (RemoteException RM) {
            System.out.println("System: The Search Module is not running" + RM.getMessage());
            return;
        }
        System.out.println("System: The Googol Aplication is running");
    }

    // LOGIN =====================================================================

    @GetMapping("/")
    public String redirect() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("user", new User());

        return "login";
    }

    @PostMapping("/save-user")
    public String saveUserSubmission(@ModelAttribute User user, Model model) {

        System.out.println(user.getName() + " " + user.getPassword());
        try {
            // Encrypt password
            String encrypted = null;
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(user.getPassword().getBytes());
            byte[] bytes = m.digest();
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                s.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            encrypted = s.toString();

            int result = SMi.login(user.getName(), encrypted);
            System.out.println();

            if (result == 1) {
                String response = "Hi " + user.getName() + ", welcome back to Googol!";
                model.addAttribute("response", response);
                return "success";
                // login = true;
            } else if (result == 0) {
                String response = "The password is incorrect";
                model.addAttribute("response", response);
                return "error";
            } else if (result == 2) {
                String response = "The username does not exists";
                model.addAttribute("response", response);
                return "error";
            }
        } catch (RemoteException e) {
            System.out.println("System: Something went wrong :(");
            System.out.println("The Search Module is not active");

        } catch (SQLException e) {
            System.out.println("System: Something went wrong :(");
            System.out.println("The DataBase is down");

        } catch (NoSuchAlgorithmException e) {
            System.out.println("System: Error encrypting password on login, no such encrypting algorithm");

        }
        return "result";
    }

    // REGISTER ==================================================================

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/save-user-register")
    public String saveRegister(@ModelAttribute User user, Model model) {
        try {
            int result = SMi.register(user.getName(), user.getPassword());
            if (result == 1) {
                String response = "User " + user.getName() + " registered successfully";
                model.addAttribute("response", response);
                return "success";

            } else if (result == 0) {
                String response = "The username chosen already exists";
                model.addAttribute("response", response);
                return "error";
            }

        } catch (RemoteException e) {
            System.out.println("System: Something went wrong :(");
            System.out.println("The Search Module is not active");

        } catch (SQLException e) {
            System.out.println("System: Something went wrong :(");
            System.out.println("The DataBase is down");

        }
        return "result";
    }

    // SEARCH WORDS ==============================================================

    @GetMapping("/search")
    public String search_words(Model model) {
        model.addAttribute("words", new Words());
        return "search_words";
    }

    @PostMapping("/see-results")
    public String Submissionresults(@ModelAttribute("words") Words words) {
        String encodedWords = words.getSearch_words().replaceAll("/", "");
        return "redirect:/search/" + encodedWords + "/" + words.getPage();
    }

    @GetMapping("/search/{searchWords}/{page}")
    public String showResultsPage(@PathVariable("searchWords") String searchWords, @PathVariable("page") int page,
            Model model) {
        // TODO: Falta fazer os botoes de next e previous
        System.out.println("searchWords -> " + "\"" + searchWords + "\"");
        model.addAttribute("words", new Words(searchWords, page));

        try {
            String[] word = searchWords.split(" ");

            org.Googol.Response response = SMi.pagesWithWord(word, page);

            if (!(response.getText().equals("\nThere are no Urls with that word!")
                    || response.getText().equals("\nThere are no active barrels!")
                    || response.getText().equals("\nThere are no more Urls with that word!"))) {
                // System.out.print(response.getText());
                String[] entries = response.getText().split("\n\n");
                // int num = page * 10;
                // System.out.println("results " + num + " / " + (num + response.getLength()));
                // System.out.println("");
                URL listUrls[] = new URL[entries.length];
                int i = 0;
                for(String s: entries){
                    String parts[] = s.split("\n");
                    URL url = new URL(parts[0].substring(5, parts[0].length()), parts[1], parts[2], null, null);
                    listUrls[i++] = url;
                    System.out.println(url.printURL());
                model.addAttribute("listUrls", listUrls);
                }
            } else {
                System.out.println(response.getText());
            }
            model.addAttribute("response", response.getText());

        } catch (RemoteException e) {
            System.out.println("System: Something went wrong :(");
            System.out.println("The Search Module is not active");
        } catch (ConcurrentModificationException e) {
            System.out.println("System: Something went wrong :(");
            System.out.println("Error Reading data from server. Restarting...");
        }
        return "results_words";
    }

    // SEARCH URL ================================================================

    @GetMapping("/search_url")
    public String search_url(Model model) {
        model.addAttribute("url", new URL_forms());
        return "search_url";
    }

    @PostMapping("/see-results-url")
    public String Submissionresults_url(@ModelAttribute("url") URL_forms url) {
        // System.out.println("link -> " + "\"" + url.getSearch_url() + "\"");
        // System.out.println("page -> " + "\"" + url.getPage() + "\"");
        try {
            String encodedUrl = URLEncoder.encode(url.getSearch_url().replace("/", "++"), "UTF-8");
            return "redirect:/search_url/" + encodedUrl + "/" + url.getPage();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "redirect:/search_url/";
    }

    @GetMapping("/search_url/{search_url}/{page}")
    public String showResultsPage_url(@PathVariable("search_url") String search_url, @PathVariable("page") int page,
            Model model) {

        try {
            // System.out.println("search_url -> " + "\"" + search_url + "\"");
            String decodedUrl = URLDecoder.decode(search_url.replace("++", "/"), "UTF-8");

            model.addAttribute("url", new URL_forms(decodedUrl, page));
            System.out.println("link -> " + "\"" + decodedUrl + "\"");
            String URL = decodedUrl;

            org.Googol.Response response = SMi.pagesWithURL(URL, page);
            if (!(response.equals("\nThere are no active barrels!") ||
                    response.equals("\nThere are no Urls with that URL!") ||
                    response.equals("\nThere are no more Urls with that URL!"))) {
                System.out.print(response.getText());
                int num = page * 10;
                System.out.println("results " + num + " / " + (num + response.getLength()));
                System.out.println("");
                // if (pages != 0)
                // System.out.println("p - Previous Page");
                // System.out.println("n - Next Page");
                // System.out.println("q - Quit Search");
                // input = scan.nextLine();
                // if (input.equals("q")) {
                // break;
                // } else if (input.equals("n")) {
                // pages++;
                // } else if (input.equals("p") && pages != 0) {
                // pages--;
                // }
            } else {
                System.out.print(response.getText());

            }
            // model.addAttribute("response", response);
            model.addAttribute("response", response.getText());

        } catch (RemoteException e) {
            System.out.println("System: Something went wrong :(");
            System.out.println("The Search Module is not active");
        } catch (ConcurrentModificationException e) {
            System.out.println("System: Something went wrong :(");
            System.out.println("Error Reading data from server. Restarting...");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "results_url";
    }

    // INDEX =====================================================================

    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("url", new URL_forms());

        return "index";
    }

    @PostMapping("/see-index")
    public String Submissionindex(@ModelAttribute URL_forms url, Model model) {

        System.out.println("link -> " + "\"" + url.getSearch_url() + "\"");

        try {
            SMi.newURL(url.getSearch_url());
            model.addAttribute("response", "Indexing completed");

        } catch (RemoteException e) {
            System.out.println("System: Something went wrong :(");
            System.out.println("The Search Module is not active");
        } catch (ConcurrentModificationException e) {
            System.out.println("System: Something went wrong :(");
            System.out.println("Error Reading data from server. Restarting...");
        }
        return "success";
    }

    // TOP SEARCHES ==============================================================

    @GetMapping("/top_searches")
    public String top_searches(Model model) {
        // model.addAttribute("words", new Words());
        return "top_searches";
    }

    // STATS =====================================================================

    @GetMapping("/stats")
    public String stats(Model model) {
        // model.addAttribute("words", new Words());
        return "stats";
    }

}