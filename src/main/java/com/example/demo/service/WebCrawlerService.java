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
        Set<String> visited = new HashSet<>();
        int count = 0;

        queue.add(normalizeUrl(url)); // Chuẩn hóa URL trước khi thêm vào hàng đợi

        while (!queue.isEmpty() && count < maxPages) {
            String currentUrl = queue.poll();

            // Chuẩn hóa mọi URL trước khi kiểm tra
            currentUrl = normalizeUrl(currentUrl);

            if (!visited.contains(currentUrl) && !webPageRepository.existsByUrl(currentUrl)) {
                try {
                    String htmlContent = fetchHtmlContent(currentUrl);
                    WebPage webPage = extractWebPage(currentUrl, htmlContent);

                    webPageRepository.save(webPage);

                    // Duyệt qua các liên kết nội bộ
                    for (String internalLink : webPage.getInternalLinks()) {
                        queue.add(internalLink);
                    }

                    // Đánh dấu URL hiện tại như đã được duyệt
                    visited.add(currentUrl);

                    // Chỉ tăng count khi một URL mới được duyệt
                    count++;
                } catch (IOException | InterruptedException e) {
                    System.out.println("Error fetching HTML content: " + e.getMessage());
                }
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
        return url;
    }
    private String fetchHtmlContent(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
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