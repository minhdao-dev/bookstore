package com.bookstore.review.service;

import com.bookstore.review.dto.RecommendationResponse;
import com.bookstore.review.repository.BookRecommendationProjection;
import com.bookstore.review.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;

    public List<RecommendationResponse> getRecommendationsForUser(UUID userId) {
        List<String> genres = recommendationRepository.findPurchasedGenres(userId);
        List<UUID> excludedIds = genres.isEmpty() ? List.of() : recommendationRepository.findPurchasedBookIds(userId);

        List<BookRecommendationProjection> results = genres.isEmpty()
                ? List.of()
                : recommendationRepository.findRecommendationsByGenres(genres, excludedIds);

        if (results.isEmpty()) {
            results = excludedIds.isEmpty()
                    ? recommendationRepository.findTopRatedBooks()
                    : recommendationRepository.findTopRatedBooksExcluding(excludedIds);
        }

        return results.stream().map(this::toResponse).toList();
    }

    private RecommendationResponse toResponse(BookRecommendationProjection projection) {
        return new RecommendationResponse(
                projection.getId(),
                projection.getTitle(),
                projection.getAuthor(),
                projection.getGenre(),
                Math.round(projection.getAvgRating() * 10) / 10.0,
                projection.getReviewCount()
        );
    }
}