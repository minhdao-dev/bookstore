package com.bookstore.review.service;

import com.bookstore.catalog.entity.Book;
import com.bookstore.catalog.repository.BookRepository;
import com.bookstore.catalog.search.BookDocument;
import com.bookstore.catalog.search.BookSearchRepository;
import com.bookstore.review.repository.BookRatingSummaryProjection;
import com.bookstore.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReindexService {

    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private final BookSearchRepository bookSearchRepository;

    public int reindexAllBooks() {
        List<Book> books = bookRepository.findAll();
        Map<UUID, BookRatingSummaryProjection> ratingsByBookId = reviewRepository.findAllRatingsSummary().stream()
                .collect(Collectors.toMap(BookRatingSummaryProjection::getBookId, r -> r));

        List<BookDocument> documents = books.stream()
                .filter(book -> book.getId() != null)
                .map(book -> {
                    UUID bookId = book.getId();
                    BookRatingSummaryProjection rating = ratingsByBookId.get(bookId);
                    double avgRating = rating != null ? rating.getAvgRating() : 0.0;
                    long reviewCount = rating != null ? rating.getReviewCount() : 0L;

                    return new BookDocument(
                            bookId, book.getTitle(), book.getAuthor(), book.getGenre(), book.getLanguage(),
                            book.getDescription(), book.getPublishedDate(), avgRating, reviewCount
                    );
                })
                .toList();

        bookSearchRepository.saveAll(documents);
        return documents.size();
    }
}