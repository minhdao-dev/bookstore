package com.bookstore.review.controller;

import com.bookstore.review.dto.RecommendationResponse;
import com.bookstore.review.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public List<RecommendationResponse> getRecommendations(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return recommendationService.getRecommendationsForUser(userId);
    }
}