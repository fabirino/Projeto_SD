package org.Googol;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.Googol.forms.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

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

}