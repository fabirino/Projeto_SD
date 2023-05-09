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

import jakarta.servlet.http.HttpSession;

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
    public String login(HttpSession session, Model model) {

        if (session.getAttribute("username") != null) {
            return "redirect:/index";
        }

        model.addAttribute("user", new User());

        return "login";
    }

    @PostMapping("/save-user")
    public String saveUserSubmission(HttpSession session, @ModelAttribute User user, Model model) {

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
                session.setAttribute("username", user.getName());
                return "redirect:/search";
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

    // LOGOUT ====================================================================
    @GetMapping("/logout")
    public String logout(HttpSession session, Model model) {
        session.invalidate();
        return "redirect:/login";
    }

    // REGISTER ==================================================================

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/save-user-register")
    public String saveRegister(HttpSession session, @ModelAttribute User user, Model model) {
        try {
            int result = SMi.register(user.getName(), user.getPassword());
            if (result == 1) {
                String response = "User " + user.getName() + " registered successfully";
                model.addAttribute("user", new User());
                session.setAttribute("username", user.getName());
                model.addAttribute("response", response);
                return "redirect:/search";

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
        return "login";
    }

    // SEARCH WORDS ==============================================================

    @GetMapping("/search")
    public String search_words(HttpSession session, Model model) {

        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        model.addAttribute("words", new Words());
        return "search_words";
    }

    @PostMapping("/see-results")
    public String Submissionresults(HttpSession session, @ModelAttribute("words") Words words) {

        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        String encodedWords = words.getSearch_words().replaceAll("/", "");
        return "redirect:/search/" + encodedWords + "/" + words.getPage();
    }

    @GetMapping("/search/{searchWords}/{page}")
    public String showResultsPage(HttpSession session, @PathVariable("searchWords") String searchWords,
            @PathVariable("page") int page,
            Model model) {

        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        // TODO: Falta fazer os botoes de next e previous
        System.out.println("searchWords -> " + "\"" + searchWords + "\"");

        try {
            String[] word = searchWords.split(" ");

            org.Googol.Response response = SMi.pagesWithWord(word, page);

            model.addAttribute("words", new Words(searchWords, page, response.getLength()));

            if (response.getLength() == 0 && response.getText().equals("\nThere are no active barrels!")) {
                model.addAttribute("response", "There are no active barrels!");
                return "error";
            } else if (response.getLength() == 0 && response.getText().equals("\nThere are no Urls with that word!")) {
                model.addAttribute("response", "There are no Urls with that word!");
                return "error";
            } else if (response.getText().equals("\nThere are no more Urls with that word!")) {
                model.addAttribute("response", "There are no more Urls with that word!");
                return "error";
            } else {
                // System.out.print(response.getText());
                String[] entries = response.getText().split("\n\n");
                // int num = page * 10;
                // System.out.println("results " + num + " / " + (num + response.getLength()));
                // System.out.println("");
                URL listUrls[] = new URL[entries.length];
                int i = 0;
                for (String s : entries) {
                    String parts[] = s.split("\n");
                    URL url = new URL(parts[0].substring(5, parts[0].length()), parts[1], parts[2], null, null);
                    listUrls[i++] = url;
                    System.out.println(url.printURL());
                }
                model.addAttribute("listUrls", listUrls);

                return "results_words";
            }

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
    public String search_url(HttpSession session, Model model) {

        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        model.addAttribute("url", new URL_forms());
        return "search_url";
    }

    @PostMapping("/see-results-url")
    public String Submissionresults_url(HttpSession session, @ModelAttribute("url") URL_forms url) {

        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        try {
            String encodedUrl = URLEncoder.encode(url.getSearch_url().replace("/", "++"), "UTF-8");
            return "redirect:/search_url/" + encodedUrl + "/" + url.getPage();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "redirect:/search_url/";
    }

    @GetMapping("/search_url/{search_url}/{page}")
    public String showResultsPage_url(HttpSession session, @PathVariable("search_url") String search_url,
            @PathVariable("page") int page,
            Model model) {

        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        try {
            // System.out.println("search_url -> " + "\"" + search_url + "\"");
            String decodedUrl = URLDecoder.decode(search_url.replace("++", "/"), "UTF-8");

            System.out.println("link -> " + "\"" + decodedUrl + "\"");
            String URL = decodedUrl;

            org.Googol.Response response = SMi.pagesWithURL(URL, page);
            model.addAttribute("url", new URL_forms(decodedUrl, page, response.getLength()));

            if (response.getLength() == 0 && response.getText().equals("\nThere are no active barrels!")) {
                model.addAttribute("response", "There are no active barrels!");
                return "error";
            } else if (response.getLength() == 0 && response.getText().equals("\nThere are no Urls with that URL!")) {
                model.addAttribute("response", "There are no Urls with that URL!");
                return "error";
            } else if (response.getText().equals("\nThere are no more Urls with that URL!")) {
                model.addAttribute("response", "There are no more Urls with that URL!");
                return "error";
            } else {

                System.out.print(response.getText());

                String[] entries = response.getText().split("\n\n\n");
                URL listUrls[] = new URL[entries.length];
                int i = 0;
                for (String s : entries) {
                    String parts[] = s.split("\n");
                    URL url = new URL(parts[0].substring(5, parts[0].length()), parts[1], parts[2], null, null);
                    listUrls[i++] = url;
                    // System.out.println(url.printURL());
                }
                model.addAttribute("listUrls", listUrls);

                return "results_url";
            }

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
    public String index(HttpSession session, Model model) {

        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        model.addAttribute("url", new URL_forms());

        return "index";
    }

    @PostMapping("/see-index")
    public String Submissionindex(HttpSession session, @ModelAttribute URL_forms url, Model model) {

        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

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

    // TOP STORIES ===============================================================

    @GetMapping("/top-stories-user")
    public String top_searches(HttpSession session, Model model) {

        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", new User());
        return "top_stories";
    }

    @PostMapping("/see-results-hackernews")
    public String results_hackernews(HttpSession session, Model model, @ModelAttribute("user") User user){
        
        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        return "redirect:/top-stories-user/" + user.getName();
    }

    @GetMapping("/top-stories-user/{name}")
    public String show_results_hackernews(HttpSession session, Model model, @PathVariable("name") String name){

        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }


        return "results_hackernews";
    }

    // STATS =====================================================================

    @GetMapping("/stats")
    public String stats(HttpSession session, Model model) {

        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        // model.addAttribute("words", new Words());
        return "stats";
    }

}