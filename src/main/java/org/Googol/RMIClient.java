package org.Googol;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.Vector;

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

        System.out.println("Welcome to Googol");

        Scanner scan = new Scanner(System.in);
        boolean continuar = true;
        int escolha = 0;
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
                                    || response.equals("\nThere are no active barrels!"))) {
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
                        Vector<String> vec = SMi.pagesWithURL(URL);
                        System.out.println("\n");
                        for (int i = 0; i < vec.size(); i++) { // TODO: INDEXAR POR PAGINAS TB,n tinha muito tempo xd
                            System.out.println(vec.get(i));
                        }
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
                return;
            } catch( SQLException e){
                System.out.println("System: Something went wrong :(");
                System.out.println("The DataBase is down");
            }
        }
        scan.close();
    }
}
