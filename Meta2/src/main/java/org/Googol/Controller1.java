package org.Googol;

import org.Googol.forms.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.ui.Model;


@Controller
public class Controller1 {
    
    @GetMapping("/")
    public String redirect(){
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Model model){
        model.addAttribute("user", new User());

        return "login";
    }

    @PostMapping("/save-user")
    public String saveUserSubmission(@ModelAttribute User user) {

        // TODO: save project in DB here
        System.out.println(user.getName() + " " + user.getPassword());

        return "result";
    }

    
}