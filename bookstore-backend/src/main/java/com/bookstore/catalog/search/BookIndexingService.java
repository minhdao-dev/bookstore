package com.bookstore.catalog.search;

import com.bookstore.catalog.entity.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookIndexingService {

    private final BookSearchRepository bookSearchRepository;

    public void indexBook(Book book) {
        UUID bookId = book.getId();
        if (bookId == null) {
            return;
        }
        try {
            BookDocument existing = bookSearchRepository.findById(bookId).orElse(null);
            double avgRating = existing != null ? existing.getAverageRating() : 0.0;
            long reviewCount = existing != null ? existing.getReviewCount() : 0L;

            BookDocument document = new BookDocument(
                    bookId, book.getTitle(), book.getAuthor(), book.getGenre(), book.getLanguage(),
                    book.getDescription(), book.getPublishedDate(), avgRating, reviewCount
            );
            bookSearchRepository.save(document);
        } catch (Exception ex) {
            log.warn("Failed to index book {} into Elasticsearch: {}", bookId, ex.getMessage());
        }
    }

    public void deleteBook(UUID bookId) {
        try {
            bookSearchRepository.deleteById(bookId);
        } catch (Exception ex) {
            log.warn("Failed to delete book {} from Elasticsearch: {}", bookId, ex.getMessage());
        }
    }

    public void updateRating(UUID bookId, double averageRating, long reviewCount) {
        try {
            bookSearchRepository.findById(bookId).ifPresent(doc -> {
                doc.setAverageRating(averageRating);
                doc.setReviewCount(reviewCount);
                bookSearchRepository.save(doc);
            });
        } catch (Exception ex) {
            log.warn("Failed to update rating for book {} in Elasticsearch: {}", bookId, ex.getMessage());
        }
    }
}