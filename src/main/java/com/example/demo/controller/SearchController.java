package com.example.demo.controller;

import com.example.demo.repo.WebPageRepository;
import com.example.demo.model.WebPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;



import java.util.*;
import java.util.stream.Collectors;
import com.example.demo.model.SearchResult;


@Controller
public class SearchController {

    @Autowired
    private WebPageRepository webPageRepository;

    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<?> search(@RequestParam String query) {
        String[] keywords = query.split("\\s+");
        List<SearchResult> searchResults = new ArrayList<>(); // Sử dụng List để lưu trữ kết quả

        // Tìm kiếm trong tiêu đề và nội dung trang web
        for (String keyword : keywords) {
            List<WebPage> results = webPageRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword);
            for (WebPage result : results) {
                SearchResult searchResult = new SearchResult(result, keywords);
                if (!containsResult(searchResults, searchResult)) {
                    searchResults.add(searchResult);
                }
            }
        }

        // Tìm kiếm theo URL
        List<WebPage> urlResults = webPageRepository.findByUrlContainingIgnoreCase(query);
        for (WebPage result : urlResults) {
            SearchResult searchResult = new SearchResult(result, keywords);
            if (!containsResult(searchResults, searchResult)) {
                searchResults.add(searchResult);
            }
        }


        // Sắp xếp danh sách kết quả
        searchResults.sort(Comparator.comparingDouble(SearchResult::getScore).reversed());

        return ResponseEntity.ok(searchResults);
    }

    // Phương thức để kiểm tra xem kết quả đã tồn tại trong danh sách chưa
    private boolean containsResult(List<SearchResult> results, SearchResult newResult) {
        for (SearchResult result : results) {
            if (result.getWebPage().getId().equals(newResult.getWebPage().getId())) {
                return true;
            }
        }
        return false;
    }
}
