package com.example.demo.service;

import com.example.demo.model.WebPage;
import com.example.demo.repo.WebPageRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;


@Service
public class WebCrawlerService {

    @Autowired
    private HttpClient httpClient;

    @Autowired
    private WebPageRepository webPageRepository;

    public void crawl(String url, int maxPages) {
        Queue<String> queue = new LinkedList<>();
        int count = 0;

        queue.add(normalizeUrl(url)); // Normalize URL before adding to the queue

        while (!queue.isEmpty() && count < maxPages) {
            String currentUrl = queue.poll();

            // Normalize every URL before checking
            currentUrl = normalizeUrl(currentUrl);
            System.out.println("Processing URL: " + currentUrl);

            if (!webPageRepository.existsByUrl(currentUrl)) {
                try {
                    String htmlContent = fetchHtmlContent(currentUrl);
                    WebPage webPage = extractWebPage(currentUrl, htmlContent);

                    webPageRepository.save(webPage);
                    System.out.println("Saved URL: " + currentUrl);

                    // Traverse internal links
                    for (String internalLink : webPage.getInternalLinks()) {
                        String normalizedInternalLink = normalizeUrl(internalLink);
                        if (!webPageRepository.existsByUrl(normalizedInternalLink)) {
                            queue.add(normalizedInternalLink);
                            System.out.println("Added internal link to queue: " + normalizedInternalLink);
                        }
                    }


                    count++;
                    System.out.println("Current count: " + count);
                } catch (IOException | InterruptedException e) {
                    System.out.println("Error fetching HTML content: " + e.getMessage());
                }
            } else {
                System.out.println("URL already exists in database: " + currentUrl);
            }
        }
    }






    // Phương thức để chuẩn hóa URL bằng cách loại bỏ các phần kí tự đặc biệt ở cuối
    private String normalizeUrl(String url) {
        // Tách URL thành phần trước và sau kí tự '#'
        int hashIndex = url.indexOf("#");
        if (hashIndex != -1) {
            url = url.substring(0, hashIndex);
        }

        // Chuẩn hóa phần cuối của URL để loại bỏ các ký tự đặc biệt như '/'
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        return url;
    }

    private String fetchHtmlContent(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private WebPage extractWebPage(String url, String htmlContent) {
        Document document = Jsoup.parse(htmlContent);

        String title = document.title();
        String content = document.body().text();
        List<String> internalLinks = new ArrayList<>();
        for (Element element : document.select("a[href]")) {
            String internalUrl = element.attr("abs:href");
            if (internalUrl.startsWith("http:") || internalUrl.startsWith("https:")) {
                internalLinks.add(internalUrl);
            }
        }

        return new WebPage(url, title, content, internalLinks);
    }

}