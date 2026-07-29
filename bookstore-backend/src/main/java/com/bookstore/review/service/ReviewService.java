package com.bookstore.review.service;

import com.bookstore.auth.entity.User;
import com.bookstore.auth.repository.UserRepository;
import com.bookstore.catalog.entity.Book;
import com.bookstore.catalog.exception.BookNotFoundException;
import com.bookstore.catalog.repository.BookRepository;
import com.bookstore.catalog.search.BookIndexingService;
import com.bookstore.order.repository.OrderLineItemRepository;
import com.bookstore.review.dto.RatingSummaryResponse;
import com.bookstore.review.dto.ReviewRequest;
import com.bookstore.review.dto.ReviewResponse;
import com.bookstore.review.dto.ReviewUpdateRequest;
import com.bookstore.review.entity.Review;
import com.bookstore.review.exception.PurchaseNotVerifiedException;
import com.bookstore.review.exception.ReviewAccessDeniedException;
import com.bookstore.review.exception.ReviewAlreadyExistsException;
import com.bookstore.review.exception.ReviewNotFoundException;
import com.bookstore.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final OrderLineItemRepository orderLineItemRepository;
    private final BookIndexingService bookIndexingService;

    public Page<ReviewResponse> getReviewsForBook(UUID bookId, Pageable pageable) {
        return reviewRepository.findByBookId(bookId, pageable).map(this::toResponse);
    }

    public RatingSummaryResponse getRatingSummary(UUID bookId) {
        long reviewCount = reviewRepository.countByBookId(bookId);
        double averageRating = reviewCount == 0
                ? 0.0
                : reviewRepository.findAverageRatingByBookId(bookId).doubleValue();
        return new RatingSummaryResponse(Math.round(averageRating * 10) / 10.0, reviewCount);
    }

    @Transactional
    public ReviewResponse createReview(UUID userId, ReviewRequest request) {
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new BookNotFoundException(request.bookId()));

        if (!orderLineItemRepository.existsPaidPurchaseByUserAndBook(userId, request.bookId())) {
            throw new PurchaseNotVerifiedException();
        }

        if (reviewRepository.findByBookIdAndUserId(request.bookId(), userId).isPresent()) {
            throw new ReviewAlreadyExistsException();
        }

        User user = userRepository.getReferenceById(userId);
        Review review = new Review(book, user, request.rating(), request.comment());
        reviewRepository.save(review);

        syncRatingToIndex(request.bookId());
        return toResponse(review);
    }

    @Transactional
    public ReviewResponse updateReview(UUID userId, UUID reviewId, ReviewUpdateRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        if (!Objects.equals(review.getUser().getId(), userId)) {
            throw new ReviewAccessDeniedException();
        }

        review.setRating(request.rating());
        review.setComment(request.comment());

        syncRatingToIndex(Objects.requireNonNull(review.getBook().getId()));
        return toResponse(review);
    }

    @Transactional
    public void deleteReview(UUID userId, UUID reviewId, boolean isAdmin) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        if (!isAdmin && !Objects.equals(review.getUser().getId(), userId)) {
            throw new ReviewAccessDeniedException();
        }

        UUID bookId = Objects.requireNonNull(review.getBook().getId());
        reviewRepository.delete(review);
        syncRatingToIndex(bookId);
    }

    private void syncRatingToIndex(UUID bookId) {
        long reviewCount = reviewRepository.countByBookId(bookId);
        double averageRating = reviewCount == 0
                ? 0.0
                : reviewRepository.findAverageRatingByBookId(bookId).doubleValue();
        bookIndexingService.updateRating(bookId, averageRating, reviewCount);
    }

    private ReviewResponse toResponse(Review review) {
        UUID id = Objects.requireNonNull(review.getId(), "Review id must not be null after persist");
        UUID bookId = Objects.requireNonNull(review.getBook().getId());
        UUID userId = Objects.requireNonNull(review.getUser().getId());

        return new ReviewResponse(
                id,
                bookId,
                review.getBook().getTitle(),
                userId,
                review.getUser().getEmail(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}