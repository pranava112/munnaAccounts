



package com.vpm.Accounts.Controller.Accounts;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vpm.Accounts.DTO.BookReportDTO;
import com.vpm.Accounts.Service.BookService;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/cash-book/{accountId}")
    public List<BookReportDTO> getCashBook(
            @PathVariable Long accountId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {

        return bookService.getCashBook(
                accountId,
                from,
                to
        );
    }

    @GetMapping("/bank-book/{accountId}")
    public List<BookReportDTO> getBankBook(
            @PathVariable Long accountId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {

        return bookService.getBankBook(
                accountId,
                from,
                to
        );
    }
}