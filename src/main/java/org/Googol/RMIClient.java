package org.Googol;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;
import java.util.Vector;

/**
 *
 * <p> Comunicam por RMI como RMISearchModule
 * <p> Tem uma interface simples com um conjunto de comandos limitados que invocam metodos remotos no servidor RMI
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
                        // scan.nextLine();
                        URL = scan.nextLine();
                        SMi.newURL(URL);
                        break;
                    case 2: //TODO: para nao esquecer, AINDA ESTA A PROCURAR POR UMA PARAVRA!
                        System.out.println("Type the Keyword you want to search:");
                        URL = scan.nextLine();
                        System.out.println(SMi.pagesWithWord(URL));

                        break;
                    case 3:
                        System.out.println("Type the URL you want to search:");
                        URL = scan.nextLine();
                        Vector<String> vec= SMi.pagesWithURL(URL);
                        System.out.println("\n");
                        for(int i=0; i< vec.size(); i++){ //TODO: INDEXAR POR PAGINAS TB,n tinha muito tempo xd 
                            System.out.println(vec.get(i));
                        }
                        break;
                    default:
                        System.out.print("Option not available, choose a number from the menu: ");
                        break;
                }
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                System.out.println("System: Something went wrong :(");
                e.printStackTrace();
            }
        }
        scan.close();
    }
}
