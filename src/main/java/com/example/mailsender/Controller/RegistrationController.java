package com.example.mailsender.Controller;

import com.example.mailsender.Entity.Users;
import com.example.mailsender.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
public class RegistrationController {



    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }


    @PostMapping("/register")
    public String registerUser(@RequestParam("username") String username,
                               @RequestParam("email") String email,
                               @RequestParam("password") String password,
                               Model model){

        Users newUser = new Users();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setConfirmationToken(UUID.randomUUID().toString());
        userRepository.save(newUser);
        sendConfirmationEmail(newUser);
        model.addAttribute("message", "Registration successful");
        return "register_result";

    }


    private void sendConfirmationEmail(Users users){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(users.getEmail());
        message.setSubject("Registration successful !");
        message.setText("To confirm your account, please click here : "
                + "http://localhost:8080/confirm-account?token=" + users.getConfirmationToken());

        mailSender.send(message);
    }

    @GetMapping("/confirm-account")
    public String confirmUserToken(@RequestParam("token") String confirmationToken, Model model)
    {
        Users users = userRepository.findByConfirmationToken(confirmationToken);

        if(users!= null){
            users.setEnabled(true);
            userRepository.save(users);
            model.addAttribute("message","Account verifed successful");

        }else {
            model.addAttribute("message", "Invalid confirmationToken");

        }
        return "confirmation_result";
    }


}
