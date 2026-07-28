package com.bookstore.review.controller;

import com.bookstore.review.dto.RatingSummaryResponse;
import com.bookstore.review.dto.ReviewRequest;
import com.bookstore.review.dto.ReviewResponse;
import com.bookstore.review.dto.ReviewUpdateRequest;
import com.bookstore.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/catalog/books/{bookId}/reviews")
    public Page<ReviewResponse> getReviewsForBook(
            @PathVariable UUID bookId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return reviewService.getReviewsForBook(bookId, pageable);
    }

    @GetMapping("/catalog/books/{bookId}/rating-summary")
    public RatingSummaryResponse getRatingSummary(@PathVariable UUID bookId) {
        return reviewService.getRatingSummary(bookId);
    }

    @PostMapping("/reviews")
    public ResponseEntity<ReviewResponse> createReview(
            Authentication authentication,
            @Valid @RequestBody ReviewRequest request
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        ReviewResponse response = reviewService.createReview(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/reviews/{reviewId}")
    public ReviewResponse updateReview(
            Authentication authentication,
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        return reviewService.updateReview(userId, reviewId, request);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            Authentication authentication,
            @PathVariable UUID reviewId
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        reviewService.deleteReview(userId, reviewId, isAdmin);
        return ResponseEntity.noContent().build();
    }
}