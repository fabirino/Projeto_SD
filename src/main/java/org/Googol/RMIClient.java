package org.Googol;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ConcurrentModificationException;
import java.util.Scanner;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;

/**
 *
 * <p>
 * Comunicam por RMI como RMISearchModule
 * <p>
 * Tem uma interface simples com um conjunto de comandos limitados que invocam
 * metodos remotos no servidor RMI
 */
public class RMIClient {

    public static void main(String[] args) {
        GoogolInterface SMi;
        try {
            SMi = (GoogolInterface) Naming.lookup("rmi://localhost:1099/SM");
        } catch (NotBoundException NBE) {
            System.out.println("System: The Interface is not bound");
            return;
        } catch (MalformedURLException MFE) {
            System.out.println("System: The URL specified is malformed");
            return;
        } catch (RemoteException RM) {
            System.out.println("System: Remote Exception catched");
            System.out.println("System: The Search Module is not running");
            return;
        }

        Scanner scan = new Scanner(System.in);
        int escolha = 0;
        String username = "";
        String password = "";
        System.out.println("Welcome to Googol");

        // Login/Register
        boolean login = false;
        while (!login) {
            System.out.println();
            System.out.println("1 - Login");
            System.out.println("2 - Register");

            // Get the right input from the user
            while (true)
                try {
                    escolha = Integer.parseInt(scan.nextLine());
                    break;
                } catch (NumberFormatException nfe) {
                    System.out.print("Option not available, choose a number from the menu: ");
                }

            try {
                int result = 0;
                switch (escolha) {
                    case 1:
                        System.out.println("Enter your username:");
                        username = scan.nextLine();
                        System.out.println("Enter your password:");
                        password = scan.nextLine();

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

                        result = SMi.login(username, encrypted);
                        System.out.println();

                        if (result == 1) {
                            System.out.println("Hi, " + username);
                            login = true;
                        } else if (result == 0) {
                            System.out.println("The password is wrong");
                        } else if (result == 2) {
                            System.out.println("The username does not exists");
                        }
                        break;
                    case 2:
                        System.out.println("Enter your username:");
                        username = scan.nextLine();
                        System.out.println("Enter your password:");
                        password = scan.nextLine();

                        result = SMi.register(username, password);
                        if (result == 1) {
                            System.out.println("Hi, " + username + ". You are now registered in Googol");
                            login = true;
                        } else if (result == 0) {
                            System.out.println("The username given already exists. Please choose another one");
                        }

                    default:
                        System.out.print("Option not available, choose a number from the menu: ");
                        break;
                }

            } catch (RemoteException e) {
                System.out.println("System: Something went wrong :(");
                System.out.println("The Search Module is not active");
                scan.close();
                return;
            } catch (SQLException e) {
                System.out.println("System: Something went wrong :(");
                System.out.println("The DataBase is down");
                scan.close();
                return;
            } catch (NoSuchAlgorithmException e) {
                System.out.println("System: Error encrypting password on login, no such encrypting algorithm");
                scan.close();
                return;
            }
        }

        escolha = 0;
        boolean continuar = true;
        String URL = "";
        String[] words = {};
        int pages = 0;
        String input = "";

        while (continuar) {
            try {
                System.out.println();
                System.out.println(SMi.menu());

                // Get the right input from the user
                while (true)
                    try {
                        escolha = Integer.parseInt(scan.nextLine());
                        break;
                    } catch (NumberFormatException nfe) {
                        System.out.print("Option not available, choose a number from the menu: ");
                    }

                switch (escolha) {
                    case 0:
                        continuar = false;
                        scan.close();
                        break;

                    case 1:
                        // Adds the URL
                        System.out.println("Type the URL you want to Index:");
                        // scan.nextLin1e();
                        URL = scan.nextLine();
                        SMi.newURL(URL);
                        break;

                    case 2:
                        System.out.println("Type the Keyword(s) you want to search for:");
                        String line = scan.nextLine();
                        words = line.split(" ");

                        while (true) {
                            String response = SMi.pagesWithWord(words, pages);
                            if (!(response.equals("\nThere are no Urls with that word!")
                                    || response.equals("\nThere are no active barrels!")
                                    || response.equals("\nThere are no more Urls with that word!"))) {
                                System.out.print(response);
                                if (pages != 0)
                                    System.out.println("p - Previous Page");
                                System.out.println("n - Next Page");
                                System.out.println("q - Quit Search");
                                input = scan.nextLine();
                                if (input.equals("q")) {
                                    break;
                                } else if (input.equals("n")) {
                                    pages++;
                                } else if (input.equals("p") && pages != 0) {
                                    pages--;
                                }
                            } else {
                                System.out.print(response);
                                break;
                            }
                        }

                        System.out.println();
                        pages = 0;
                        break;

                    case 3:
                        System.out.println("Type the URL you want to search:");
                        URL = scan.nextLine();
                        while (true) {
                            String response = SMi.pagesWithURL(URL, pages);
                            if (!(response.equals("\nThere are no active barrels!") ||
                                    response.equals("\nThere are no Urls with that URL!") ||
                                    response.equals("\nThere are no more Urls with that URL!"))) {
                                System.out.println(response);
                                if (pages != 0)
                                    System.out.println("p - Previous Page");
                                System.out.println("n - Next Page");
                                System.out.println("q - Quit Search");
                                input = scan.nextLine();
                                if (input.equals("q")) {
                                    break;
                                } else if (input.equals("n")) {
                                    pages++;
                                } else if (input.equals("p") && pages != 0) {
                                    pages--;
                                }
                            } else {
                                System.out.print(response);
                                break;
                            }
                        }
                        System.out.println();
                        pages = 0;
                        break;
                    case 4:
                        System.out.println(SMi.adminPage());
                        break;
                    default:
                        System.out.print("Option not available, choose a number from the menu: ");
                        break;
                }
            } catch (RemoteException e) {
                System.out.println("System: Something went wrong :(");
                System.out.println("The Search Module is not active");
                // e.printStackTrace();
                scan.close();
                return;
            } catch (SQLException e) {
                System.out.println("System: Something went wrong :(");
                System.out.println("The DataBase is down");
                scan.close();
                return;
            } catch (ConcurrentModificationException e) {
                System.out.println("System: Something went wrong :(");
                System.out.println("Error Reading data from server. Restarting...");
                ;
            }
        }
        scan.close();
    }
}
