package com.bookstore.catalog.service;

import com.bookstore.catalog.dto.*;
import com.bookstore.catalog.entity.Book;
import com.bookstore.catalog.entity.ProductType;
import com.bookstore.catalog.entity.ProductVariant;
import com.bookstore.catalog.entity.VariantStatus;
import com.bookstore.catalog.exception.BookInUseException;
import com.bookstore.catalog.exception.BookNotFoundException;
import com.bookstore.catalog.exception.InvalidProductVariantException;
import com.bookstore.catalog.exception.ProductVariantInUseException;
import com.bookstore.catalog.exception.ProductVariantNotFoundException;
import com.bookstore.catalog.repository.BookRepository;
import com.bookstore.catalog.repository.ProductVariantRepository;
import com.bookstore.catalog.search.BookIndexingService;
import com.bookstore.catalog.search.BookSearchProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final BookRepository bookRepository;
    private final ProductVariantRepository productVariantRepository;
    private final BookSearchProvider bookSearchProvider;
    private final BookIndexingService bookIndexingService;

    public CatalogService(BookRepository bookRepository,
                          ProductVariantRepository productVariantRepository,
                          BookSearchProvider bookSearchProvider,
                          BookIndexingService bookIndexingService) {
        this.bookRepository = bookRepository;
        this.productVariantRepository = productVariantRepository;
        this.bookSearchProvider = bookSearchProvider;
        this.bookIndexingService = bookIndexingService;
    }

    public Page<BookResponse> search(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return bookRepository.findAll(pageable).map(this::toResponse);
        }

        Page<UUID> idPage = bookSearchProvider.search(keyword, pageable);
        Map<UUID, Book> bookById = bookRepository.findAllById(idPage.getContent()).stream()
                .collect(Collectors.toMap(book -> Objects.requireNonNull(book.getId()), book -> book));

        List<BookResponse> ordered = idPage.getContent().stream()
                .map(bookById::get)
                .filter(Objects::nonNull)
                .map(this::toResponse)
                .toList();

        return new PageImpl<>(ordered, pageable, idPage.getTotalElements());
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
        bookIndexingService.indexBook(book);
        return toResponse(book);
    }

    @Transactional
    public BookResponse update(UUID bookId, BookRequest request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
        applyRequest(book, request);
        bookIndexingService.indexBook(book);
        return toResponse(book);
    }

    @Transactional
    public void delete(UUID bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new BookNotFoundException(bookId);
        }
        try {
            bookRepository.deleteById(bookId);
            bookRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new BookInUseException(bookId);
        }
        bookIndexingService.deleteBook(bookId);
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
    public ProductVariantResponse updateVariantStatus(UUID variantId, VariantStatus status) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ProductVariantNotFoundException(variantId));
        variant.setStatus(status);
        return toVariantResponse(variant);
    }

    @Transactional
    public void deleteVariant(UUID variantId) {
        if (!productVariantRepository.existsById(variantId)) {
            throw new ProductVariantNotFoundException(variantId);
        }
        try {
            productVariantRepository.deleteById(variantId);
            productVariantRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ProductVariantInUseException(variantId);
        }
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
        if (request.productType() != ProductType.PHYSICAL
                && (request.weight() != null || request.dimensions() != null)) {
            throw new InvalidProductVariantException("Weight and dimensions are only allowed for PHYSICAL variants");
        }

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