package com.bookstore.catalog.controller;

import com.bookstore.catalog.dto.BookRequest;
import com.bookstore.catalog.dto.BookResponse;
import com.bookstore.catalog.dto.ProductVariantRequest;
import com.bookstore.catalog.dto.ProductVariantResponse;
import com.bookstore.catalog.dto.VariantStatusUpdateRequest;
import com.bookstore.catalog.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/catalog/books")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public Page<BookResponse> search(@RequestParam(required = false) String keyword,
                                     Pageable pageable) {
        return catalogService.search(keyword, pageable);
    }

    @GetMapping("/{id}")
    public BookResponse getById(@PathVariable UUID id) {
        return catalogService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BookResponse update(@PathVariable UUID id, @Valid @RequestBody BookRequest request) {
        return catalogService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        catalogService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{bookId}/variants")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductVariantResponse> addVariant(@PathVariable UUID bookId,
                                                             @Valid @RequestBody ProductVariantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.addVariant(bookId, request));
    }

    @PutMapping("/variants/{variantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductVariantResponse updateVariant(@PathVariable UUID variantId,
                                                @Valid @RequestBody ProductVariantRequest request) {
        return catalogService.updateVariant(variantId, request);
    }

    @PatchMapping("/variants/{variantId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductVariantResponse updateVariantStatus(@PathVariable UUID variantId,
                                                      @Valid @RequestBody VariantStatusUpdateRequest request) {
        return catalogService.updateVariantStatus(variantId, request.status());
    }

    @DeleteMapping("/variants/{variantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVariant(@PathVariable UUID variantId) {
        catalogService.deleteVariant(variantId);
        return ResponseEntity.noContent().build();
    }
}