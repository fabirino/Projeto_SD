package org.Googol;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.Googol.forms.User;
import org.Googol.forms.Words;
import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

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
    public String saveUserSubmission(@ModelAttribute User user) {

        // TODO: save project in DB here
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
                System.out.println("Hi, " + user.getName());
                // login = true;
            } else if (result == 0) {
                System.out.println("The password is wrong");
            } else if (result == 2) {
                System.out.println("The username does not exists");
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

    @GetMapping("/search")
    public String search_words(Model model) {
        model.addAttribute("words", new Words());

        return "search_words";
    }

    @PostMapping("/see-results")
    public String Submissionresults( @ModelAttribute Words words, Model model) {

        // TODO: Falta fazer os botoes de next e previous 
        System.out.println("words -> " + "\"" + words.getSearch_words() + "\"");
        
        try {
            String[] word = words.getSearch_words().split(" ");
            int pages = 0;

            // while (true) {
                String response = SMi.pagesWithWord(word, pages);

                if (!(response.equals("\nThere are no Urls with that word!")
                        || response.equals("\nThere are no active barrels!")
                        || response.equals("\nThere are no more Urls with that word!"))) {
                    System.out.print(response);
                    if (pages != 0)
                        System.out.println("p - Previous Page");
                    System.out.println("n - Next Page");
                    System.out.println("q - Quit Search");
                    // input = scan.nextLine();
                    // if (input.equals("q")) {
                    //     break;
                    // } else if (input.equals("n")) {
                    //     pages++;
                    // } else if (input.equals("p") && pages != 0) {
                    //     pages--;
                    // }
                } else {
                    System.out.println(response);
                    // break;
                }
                model.addAttribute("response", response);
            // }
        } catch (RemoteException e) {
            System.out.println("System: Something went wrong :(");
            System.out.println("The Search Module is not active");
        } catch (ConcurrentModificationException e) {
            System.out.println("System: Something went wrong :(");
            System.out.println("Error Reading data from server. Restarting...");
        }
        return "results_words";
    }

    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("words", new Words());

        return "index";
    }

    @PostMapping("/see-index")
    public String Submissionindex( @ModelAttribute Words words, Model model) {

        System.out.println("link -> " + "\"" + words.getSearch_words() + "\"");
        
        try {
                SMi.newURL(words.getSearch_words());
                model.addAttribute("response", "Indexing completed");
        } catch (RemoteException e) {
            System.out.println("System: Something went wrong :(");
            System.out.println("The Search Module is not active");
        } catch (ConcurrentModificationException e) {
            System.out.println("System: Something went wrong :(");
            System.out.println("Error Reading data from server. Restarting...");
        }
        return "testing_index";
    }

}