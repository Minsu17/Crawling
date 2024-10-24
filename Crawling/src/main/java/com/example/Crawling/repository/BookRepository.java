package com.example.Crawling.repository;

import com.example.Crawling.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    // 중복된 제목을 가진 책들을 모두 반환
    List<Book> findByTitle(String title);
}