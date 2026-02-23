package com.example.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

@SpringBootTest
class PetAdoptionApplicationTests {

    @Autowired
    MailSender mailSender;

    @Test
    void contextLoads() {
    }

    @Test
    void testMailSender() {
        SimpleMailMessage testMail = new SimpleMailMessage();
        testMail.setFrom("lqjhzp@163.com");
        testMail.setTo("lqjhzp@163.com");
        testMail.setSubject("测试邮件");
        testMail.setText("测试邮件内容");
        mailSender.send(testMail);
    }
}
