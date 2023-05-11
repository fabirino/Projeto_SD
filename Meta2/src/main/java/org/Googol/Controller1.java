package org.Googol;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.Googol.forms.Stories_forms;
import org.Googol.forms.URL_forms;
import org.Googol.forms.User;
import org.Googol.forms.Words;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpSession;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONObject;
import org.json.JSONArray;

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
                model.addAttribute("url", new URL_forms());

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
        return "top_stories_user";
    }

    @PostMapping("/see-results-hackernews-user")
    public String results_hackernews_user(HttpSession session, Model model, @ModelAttribute("user") User user) {

        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        return "redirect:/top-stories-user/" + user.getName();
    }

    @GetMapping("/top-stories-user/{name}")
    public String show_results_hackernews_user(HttpSession session, Model model, @PathVariable("name") String name) {

        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        try {

            // connect to the stories of an Hackernews user
            String link = "https://hacker-news.firebaseio.com/v0/user/" + name + ".json?print=pretty";
            URI uri = new URI(link);
            HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();

            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();

            // Verify if connection was successful
            if (responseCode != HttpURLConnection.HTTP_OK) {
                String response = "Something went wrong with the API request";
                // FIXME: mostrar o erro
                // model.addAttribute("error_code", responseCode);
                model.addAttribute("response", response);
                return "error";
            }

            // Read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Verify if the user exists
            if (response.toString().equals("null")) {
                String response2 = "The user does not exist";
                model.addAttribute("response", response2);
                return "error";
            }

            // Transform the response into a JSON object
            JSONObject jsonObject = new JSONObject(response.toString());
            JSONArray jsonArray = jsonObject.getJSONArray("submitted");
            con.disconnect();
            // Get the stories of the user and index them
            int[] stories = new int[jsonArray.length()];
            ConcurrentLinkedQueue<Stories_forms> stories_forms = new ConcurrentLinkedQueue<Stories_forms>();
            for (int i = 0; i < jsonArray.length(); i++) {
                stories[i] = jsonArray.getInt(i);

                String story_link = "https://hacker-news.firebaseio.com/v0/item/" + stories[i] + ".json?print=pretty";
                URI uri2 = new URI(story_link);
                HttpURLConnection con2 = (HttpURLConnection) uri2.toURL().openConnection();
                con2.setRequestMethod("GET");
                int responseCode2 = con2.getResponseCode();

                if (responseCode2 != HttpURLConnection.HTTP_OK) {
                    String response2 = "Something went wrong with the API request (after the user request))";
                    // FIXME: mostrar o erro
                    // model.addAttribute("error_code", responseCode);
                    model.addAttribute("response", response2);
                    return "error";
                }

                BufferedReader in2 = new BufferedReader(new InputStreamReader(con2.getInputStream()));
                String inputLine2;
                StringBuilder response2 = new StringBuilder();

                while ((inputLine2 = in2.readLine()) != null) {
                    response2.append(inputLine2);
                }
                in2.close();

                JSONObject jsonObject2 = new JSONObject(response2.toString());

                String type = jsonObject2.getString("type");
                System.out.println(stories[i] + " " + type);
                if (type.equals("story")) {
                    String url = jsonObject2.getString("url");
                    String title = jsonObject2.getString("title");
                    int score = jsonObject2.getInt("score");
                    long timestamp = jsonObject2.getLong("time");
                    Date date = new Date(timestamp * 1000L); // Convert seconds to milliseconds
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    String formattedDate = sdf.format(date);

                    Stories_forms story = new Stories_forms(url, title, score, formattedDate, stories[i]);
                    stories_forms.add(story);
                    // System.out.println(story);

                    SMi.newURL(url);
                }

                con2.disconnect();
            }

            model.addAttribute("stories", stories_forms);

            return "results_hackernews";

        } catch (URISyntaxException e) {

        } catch (RemoteException e) {
            System.out.println("System: Something went wrong :(");
            System.out.println("The Search Module is not active");
        } catch (IOException e) {

        }

        // Get request to this api link
        // se devolver null o user nao existe -> mostrar pagina de erro

        return "results_hackernews";
    }

    @GetMapping("/top-stories")
    public String top_stories(HttpSession session, Model model) {

        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", new User());
        return "top_stories";
    }

    @PostMapping("/see-results-hackernews")
    public String results_hackernews(HttpSession session, Model model, @ModelAttribute("user") User user) {

        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        return "redirect:/top-stories/" + user.getName();
    }

    @GetMapping("/top-stories/{search}")
    public String show_results_hackernews(HttpSession session, Model model, @PathVariable("search") String search) {

        if (session.getAttribute("username") == null) {
            return "redirect:/login";
        }

        try {

            // connect to the stories of an Hackernews user
            String link = "https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty";
            URI uri = new URI(link);
            HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();

            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();

            // Verify if connection was successful
            if (responseCode != HttpURLConnection.HTTP_OK) {
                String response = "Something went wrong with the API request";
                // FIXME: mostrar o erro
                // model.addAttribute("error_code", responseCode);
                model.addAttribute("response", response);
                return "error";
            }

            // Read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Verify if the user exists
            if (response.toString().equals("null")) {
                String response2 = "Top Stories does not exist";
                model.addAttribute("response", response2);
                return "error";
            }

            String[] stories= response.toString().replace("[", "").replace("]", "").replace(" ", "").split(",");
            
            con.disconnect();
            // Get the stories of the user and index them
            
            ConcurrentLinkedQueue<Stories_forms> stories_forms = new ConcurrentLinkedQueue<Stories_forms>();
            int max = stories.length;
            if (stories.length > 100)
                max = 100;
            for (int i = 0; i < max; i++) {
                
                String story_link = "https://hacker-news.firebaseio.com/v0/item/" + stories[i] + ".json?print=pretty";
                URI uri2 = new URI(story_link);
                HttpURLConnection con2 = (HttpURLConnection) uri2.toURL().openConnection();
                con2.setRequestMethod("GET");
                int responseCode2 = con2.getResponseCode();
                
                if (responseCode2 != HttpURLConnection.HTTP_OK) {
                    String response2 = "Something went wrong with the API request (after the user request))";
                    // FIXME: mostrar o erro
                    // model.addAttribute("error_code", responseCode);
                    model.addAttribute("response", response2);
                    return "error";
                }
                BufferedReader in2 = new BufferedReader(new InputStreamReader(con2.getInputStream()));
                String inputLine2;
                StringBuilder response2 = new StringBuilder();
                
                while ((inputLine2 = in2.readLine()) != null) {
                    response2.append(inputLine2);
                }
                
                in2.close();
                
                JSONObject jsonObject2 = new JSONObject(response2.toString());

                String type = jsonObject2.getString("type");
                System.out.println(stories[i] + " " + type);

                // check if the story contains the search words
                if (type.equals("story")) {
                    if (!jsonObject2.has("title")) {
                        if (!jsonObject2.has("text")) {
                            continue;
                        } else {
                            if (jsonObject2.getString("text").toLowerCase().contains(search.toLowerCase())) {
                                String url = jsonObject2.getString("url");
                                String title = jsonObject2.getString("text");
                                int score = jsonObject2.getInt("score");
                                long timestamp = jsonObject2.getLong("time");
                                Date date = new Date(timestamp * 1000L); // Convert seconds to milliseconds
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                String formattedDate = sdf.format(date);

                                Stories_forms story = new Stories_forms(url, title, score, formattedDate, Integer.parseInt(stories[i]));
                                stories_forms.add(story);
                                // System.out.println(story);

                                SMi.newURL(url);
                            }
                        }
                    } else if (!jsonObject2.has("text")) {
                        if (!jsonObject2.has("title")) {
                            continue;
                        } else {
                            if (jsonObject2.getString("title").toLowerCase().contains(search.toLowerCase())) {
                                String url = jsonObject2.getString("url");
                                String title = jsonObject2.getString("title");
                                int score = jsonObject2.getInt("score");
                                long timestamp = jsonObject2.getLong("time");
                                Date date = new Date(timestamp * 1000L); // Convert seconds to milliseconds
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                String formattedDate = sdf.format(date);

                                Stories_forms story = new Stories_forms(url, title, score, formattedDate, Integer.parseInt(stories[i]));
                                stories_forms.add(story);
                                // System.out.println(story);

                                SMi.newURL(url);
                            }
                        }
                    } else if (jsonObject2.getString("title").toLowerCase().contains(search.toLowerCase())
                            || jsonObject2.getString("text").toLowerCase().contains(search.toLowerCase())) {
                        String url = jsonObject2.getString("url");
                        String title = jsonObject2.getString("title");
                        int score = jsonObject2.getInt("score");
                        long timestamp = jsonObject2.getLong("time");
                        Date date = new Date(timestamp * 1000L); // Convert seconds to milliseconds
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        String formattedDate = sdf.format(date);

                        Stories_forms story = new Stories_forms(url, title, score, formattedDate, Integer.parseInt(stories[i]));
                        stories_forms.add(story);
                        // System.out.println(story);

                        SMi.newURL(url);
                    }
                }
                con2.disconnect();
            }

            model.addAttribute("stories", stories_forms);
            

            return "results_search_hackernews";

        } catch (URISyntaxException e) {

        } catch (RemoteException e) {
            System.out.println("System: Something went wrong :(");
            System.out.println("The Search Module is not active");
        } catch (IOException e) {

        }

        // Get request to this api link
        // se devolver null o user nao existe -> mostrar pagina de erro

        return "results_search_hackernews";
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