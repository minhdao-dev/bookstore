package com.bookstore.content.controller;

import com.bookstore.content.dto.ContentAccessResponse;
import com.bookstore.content.entity.ContentType;
import com.bookstore.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @PostMapping("/variants/{variantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void uploadContent(
            @PathVariable UUID variantId,
            @RequestParam ContentType contentType,
            @RequestParam("file") MultipartFile file
    ) {
        contentService.uploadContent(variantId, contentType, file);
    }

    @GetMapping("/variants/{variantId}/access-url")
    public ContentAccessResponse getAccessUrl(
            Authentication authentication,
            @PathVariable UUID variantId
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        return contentService.getAccessUrl(userId, variantId);
    }

    @GetMapping("/variants/{variantId}/hls/{fileName}")
    public ResponseEntity<byte[]> getHlsFile(
            Authentication authentication,
            @PathVariable UUID variantId,
            @PathVariable String fileName
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        return contentService.getHlsFile(userId, variantId, fileName);
    }
}