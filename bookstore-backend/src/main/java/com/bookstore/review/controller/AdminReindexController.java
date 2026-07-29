package com.bookstore.review.controller;

import com.bookstore.review.service.ReindexService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/catalog")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReindexController {

    private final ReindexService reindexService;

    @PostMapping("/reindex")
    public Map<String, Integer> reindex() {
        int count = reindexService.reindexAllBooks();
        return Map.of("indexed", count);
    }
}