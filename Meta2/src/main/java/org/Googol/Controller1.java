package org.Googol;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class Controller1 {
    
    @GetMapping("/")
    public String redirect(){
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/search_url")
    public String search_url(){
        return "search_url";
    }

    
}