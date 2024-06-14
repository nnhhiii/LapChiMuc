package com.example.demo.model;

import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Getter
public class SearchResult {
    private WebPage webPage;
    private double score;

    public SearchResult(WebPage webPage, String[] keywords) {
        this.webPage = webPage;
        this.score = calculateScore(webPage, keywords);
    }

    private double calculateScore(WebPage webPage, String[] keywords) {
        double titleScore = calculateScoreForKeywords(webPage.getTitle(), keywords, 10.0); // Trọng số cho tiêu đề là 2.0
        double contentScore = calculateScoreForKeywords(webPage.getContent(), keywords, 1.0); // Trọng số cho nội dung là 1.0
        double urlScore = calculateScoreForKeywords(webPage.getUrl(), keywords, 20.0); // Trọng số cho URL là 20.0

        // Tính điểm số tổng cộng, với ưu tiên cao hơn cho tiêu đề và URL
        return titleScore + contentScore + urlScore;
    }

    private double calculateScoreForKeywords(String text, String[] keywords, double weight) {
        double score = 0;
        for (String keyword : keywords) {
            int count = StringUtils.countOccurrencesOf(text.toLowerCase(), keyword.toLowerCase());
            score += count * weight; // Nhân với trọng số
        }
        // Bạn có thể thêm điều kiện để kiểm tra trùng khớp chính xác và tăng điểm số thêm nữa
        if (text.equalsIgnoreCase(String.join(" ", keywords))) {
            score *= 10; // Giả sử tăng điểm số lên 10 lần nếu có trùng khớp chính xác
        }
        return score;
    }
}