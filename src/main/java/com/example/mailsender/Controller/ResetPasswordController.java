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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class ResetPasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot_password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        Users user = userRepository.findByEmail(email);

        if (user != null) {
            user.setConfirmationToken(UUID.randomUUID().toString());
            userRepository.save(user);
            sendPasswordResetEmail(user);
            model.addAttribute("message", "Password reset link sent! Please check your email.");
        } else {
            model.addAttribute("message", "Email not found.");
        }

        return "forgot_password_result";
    }

    private void sendPasswordResetEmail(Users user) {
        String encodedToken = "";
        try {
            // Кодирование токена для корректной передачи через URL
            encodedToken = URLEncoder.encode(user.getConfirmationToken(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Обработка исключений, если возникнут проблемы с кодировкой
            e.printStackTrace();
        }

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("Password reset request");
        mailMessage.setText("To reset your password, click here: " +
                "http://localhost:8080/reset-password?token=" + encodedToken);

        // Отправляем письмо с кодированным токеном
        mailSender.send(mailMessage);
    }


    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        Users user = userRepository.findByConfirmationToken(token);

        if (user != null) {
            model.addAttribute("token", token);
            return "reset_password";
        } else {
            model.addAttribute("message", "Invalid password reset token.");
            return "reset_password_result";
        }
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("token") String token,
                                @RequestParam("password") String password,
                                Model model) {
        // Логирование перед началом процесса сброса пароля
        System.out.println("Received password reset request with token: " + token);

        // Находим пользователя по токену
        Users user = userRepository.findByConfirmationToken(token);

        if (user != null) {
            System.out.println("Found user with email: " + user.getEmail());

            // Проверка срока действия токена
            if (user.getTokenCreationDate() != null &&
                    user.getTokenCreationDate().isBefore(LocalDateTime.now().minusHours(1))) {
                model.addAttribute("message", "Token has expired. Please request a new password reset.");
                return "reset_password_result";
            }

            // Логирование перед изменением пароля и сбросом токена
            System.out.println("Resetting password for user: " + user.getEmail());

// Сбрасываем токен
            user.setConfirmationToken(null);
            // Устанавливаем новый пароль
            user.setPassword(password);



            // Сохраняем пользователя с новым паролем и сброшенным токеном
            userRepository.save(user);
            model.addAttribute("message", "Password reset successfully!");
        } else {
            System.out.println("No user found with token: " + token);
            model.addAttribute("message", "Invalid password reset token.");
        }

        return "reset_password_result";
    }







}



