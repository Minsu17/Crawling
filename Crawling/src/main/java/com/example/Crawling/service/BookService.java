package com.example.Crawling.service;

import com.example.Crawling.entity.Book;
import com.example.Crawling.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    // URL 목록
    private static final String[] URLS = {
            "https://www.ypbooks.co.kr/books/domestic/search?idKey=118&upIdKey=117&nationType=korea",
            "https://www.ypbooks.co.kr/books/domestic/search?idKey=128&nationType=korea&upIdKey=117",
            "https://www.ypbooks.co.kr/books/domestic/search?idKey=165&nationType=korea&upIdKey=117",
            "https://www.ypbooks.co.kr/books/domestic/search?idKey=147&nationType=korea&upIdKey=117",
            "https://www.ypbooks.co.kr/books/domestic/search?idKey=174&nationType=korea&upIdKey=117"
    };

    public void crawlAndSaveBooks() {
        System.setProperty("webdriver.chrome.driver", "C:/uplus/chromedriver-win64/chromedriver.exe");
        WebDriver driver = new ChromeDriver();

        try {
            for (String url : URLS) {
                driver.get(url);
                Thread.sleep(5000); // 페이지 로딩 대기

                // 판매량 순 정렬 버튼 클릭
                try {
                    WebElement salesSortButton = driver.findElement(By.cssSelector("#content > div.inner > div.container > div.section.section--best-select > div > div:nth-child(1) > ul > li:nth-child(1) > a"));
                    salesSortButton.click();
                    Thread.sleep(5000);  // 판매량 순으로 정렬 후 대기
                } catch (Exception e) {
                    System.out.println("Sales sort button not found on URL: " + url);
                }

                // 1페이지의 책 목록만 크롤링
                String pageSource = driver.getPageSource();
                Document document = Jsoup.parse(pageSource);
                Elements bookElements = document.select("#content > div.inner > div.container > div.section.section--best-seller > ul > li");

                List<Book> books = new ArrayList<>();

                for (Element bookElement : bookElements) {
                    String title = bookElement.select("div.group > div:nth-child(1) > div > div.book__content > a").text();
                    String author = bookElement.select("div.group > div:nth-child(1) > div > div.book__content > ul.list-info__column > li.list-info__writer > span").text();
                    String publisher = bookElement.select("div.group > div:nth-child(1) > div > div.book__content > ul.list-info__column > li:nth-child(2) > ul > li:nth-child(1) > span").text();
                    String imageUrl = bookElement.select("div.group > div:nth-child(1) > div > div.book__control > a > img").attr("src");

                    // 중복된 제목이 있을 경우 처리
                    List<Book> existingBooks = bookRepository.findByTitle(title);
                    if (existingBooks.isEmpty()) {
                        Book book = new Book();
                        book.setTitle(title);
                        book.setAuthor(author);
                        book.setPublisher(publisher);
                        book.setImageUrl(imageUrl);

                        books.add(book);
                        System.out.println("Saving new book: " + title);
                    } else {
                        System.out.println("Book already exists: " + title);
                    }
                }

                // 저장된 책들이 있으면 DB에 저장
                if (!books.isEmpty()) {
                    bookRepository.saveAll(books);
                    System.out.println("Books from the current page on URL: " + url + " successfully saved to database.");
                } else {
                    System.out.println("No new books to save on the current page of URL: " + url);
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
