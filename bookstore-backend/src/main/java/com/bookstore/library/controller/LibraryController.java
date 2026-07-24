package com.bookstore.library.controller;

import com.bookstore.library.dto.LibraryItemResponse;
import com.bookstore.library.dto.UpdateProgressRequest;
import com.bookstore.library.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    @GetMapping
    public List<LibraryItemResponse> getLibrary(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return libraryService.getLibrary(userId);
    }

    @PutMapping("/variants/{variantId}/progress")
    public void updateProgress(
            Authentication authentication,
            @PathVariable UUID variantId,
            @RequestBody UpdateProgressRequest request
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        libraryService.updateProgress(userId, variantId, request);
    }
}