package com.example.demo.repo;

import com.example.demo.model.WebPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebPageRepository extends JpaRepository<WebPage, Long> {
    List<WebPage> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String titleKeyword, String contentKeyword);
    List<WebPage> findByUrlContainingIgnoreCase(String urlKeyword);
    boolean existsByUrl(String url);
}



