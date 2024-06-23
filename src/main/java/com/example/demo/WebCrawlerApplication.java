package com.example.demo;

import com.example.demo.service.WebCrawlerService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class WebCrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebCrawlerApplication.class, args);
    }

    @Autowired
    private WebCrawlerService webCrawlerService;

    @PostConstruct
    public void startCrawling() {
        webCrawlerService.crawl("https://vnexpress.net/", 50);
    }
}

