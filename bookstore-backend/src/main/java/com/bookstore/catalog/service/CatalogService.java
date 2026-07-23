package com.bookstore.catalog.service;

import com.bookstore.catalog.dto.*;
import com.bookstore.catalog.entity.Book;
import com.bookstore.catalog.entity.ProductVariant;
import com.bookstore.catalog.entity.VariantStatus;
import com.bookstore.catalog.exception.BookNotFoundException;
import com.bookstore.catalog.exception.ProductVariantNotFoundException;
import com.bookstore.catalog.repository.BookRepository;
import com.bookstore.catalog.repository.ProductVariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final BookRepository bookRepository;
    private final ProductVariantRepository productVariantRepository;

    public CatalogService(BookRepository bookRepository,
                          ProductVariantRepository productVariantRepository) {
        this.bookRepository = bookRepository;
        this.productVariantRepository = productVariantRepository;
    }

    public Page<BookResponse> search(String keyword, Pageable pageable) {
        Page<Book> books = (keyword == null || keyword.isBlank())
                ? bookRepository.findAll(pageable)
                : bookRepository.searchByKeyword(keyword, pageable);

        return books.map(this::toResponse);
    }

    public BookResponse getById(UUID bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
        return toResponse(book);
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        Book book = new Book();
        applyRequest(book, request);
        bookRepository.save(book);
        return toResponse(book);
    }

    @Transactional
    public BookResponse update(UUID bookId, BookRequest request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
        applyRequest(book, request);
        return toResponse(book);
    }

    @Transactional
    public void delete(UUID bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new BookNotFoundException(bookId);
        }
        bookRepository.deleteById(bookId);
    }

    @Transactional
    public ProductVariantResponse addVariant(UUID bookId, ProductVariantRequest request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        ProductVariant variant = new ProductVariant();
        variant.setBook(book);
        applyVariantRequest(variant, request);
        variant.setStatus(VariantStatus.ACTIVE);
        productVariantRepository.save(variant);

        return toVariantResponse(variant);
    }

    @Transactional
    public ProductVariantResponse updateVariant(UUID variantId, ProductVariantRequest request) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ProductVariantNotFoundException(variantId));
        applyVariantRequest(variant, request);
        return toVariantResponse(variant);
    }

    @Transactional
    public void deleteVariant(UUID variantId) {
        if (!productVariantRepository.existsById(variantId)) {
            throw new ProductVariantNotFoundException(variantId);
        }
        productVariantRepository.deleteById(variantId);
    }

    private void applyRequest(Book book, BookRequest request) {
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setGenre(request.genre());
        book.setLanguage(request.language());
        book.setDescription(request.description());
        book.setPublishedDate(request.publishedDate());
    }

    private void applyVariantRequest(ProductVariant variant, ProductVariantRequest request) {
        variant.setProductType(request.productType());
        variant.setVariantFormat(request.variantFormat());
        variant.setSku(request.sku());
        variant.setPrice(request.price());
        variant.setCurrency(request.currency());
        variant.setWeight(request.weight());
        variant.setDimensions(request.dimensions());
    }

    private BookResponse toResponse(Book book) {
        UUID bookId = Objects.requireNonNull(book.getId(), "Persisted book must have an id");

        List<ProductVariantResponse> variants = productVariantRepository
                .findByBookId(bookId)
                .stream()
                .map(this::toVariantResponse)
                .toList();

        return new BookResponse(
                bookId, book.getTitle(), book.getAuthor(), book.getGenre(),
                book.getLanguage(), book.getDescription(), book.getPublishedDate(), variants
        );
    }

    private ProductVariantResponse toVariantResponse(ProductVariant variant) {
        UUID variantId = Objects.requireNonNull(variant.getId(), "Persisted variant must have an id");

        return new ProductVariantResponse(
                variantId, variant.getProductType(), variant.getVariantFormat(),
                variant.getSku(), variant.getPrice(), variant.getCurrency(),
                variant.getWeight(), variant.getDimensions(), variant.getStatus()
        );
    }
}