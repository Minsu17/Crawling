package com.example.Crawling.controller;

import com.example.Crawling.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    // "/crawl-books" 경로로 GET 요청이 오면 크롤링을 수행하고 데이터베이스에 저장
    @GetMapping("/crawl-books")
    public String crawlBooks() {
        bookService.crawlAndSaveBooks();
        return "Books have been successfully crawled and saved!";
    }
}
