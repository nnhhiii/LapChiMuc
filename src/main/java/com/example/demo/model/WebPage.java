package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "web_pages")
public class WebPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;
    private String title;
    private String content;
    @ElementCollection
    @CollectionTable(name = "web_page_internal_links", joinColumns = @JoinColumn(name = "web_page_id"))
    @Column(name = "internal_link")
    private List<String> internalLinks;

    public WebPage(String url,String title, String content, List<String> internalLinks) {
        this.url = url;
        this.title = title;
        this.content = content;
        this.internalLinks = internalLinks;
    }
}