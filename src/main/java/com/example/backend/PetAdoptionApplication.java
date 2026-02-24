package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mail.MailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class PetAdoptionApplication {

    public static void main(String[] args) {
        var context = SpringApplication.run(PetAdoptionApplication.class, args);
        PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);
        System.out.println("Password `admin` is " + passwordEncoder.encode("admin"));
    }
}
