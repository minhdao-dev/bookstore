CREATE TABLE content_asset
(
    id                 UUID PRIMARY KEY,
    product_variant_id UUID         NOT NULL REFERENCES product_variant (id),
    content_type       VARCHAR(20)  NOT NULL CHECK (content_type IN ('EPUB', 'PDF', 'MP3', 'M4B')),
    storage_key        VARCHAR(500) NOT NULL,
    file_size_bytes    BIGINT,
    uploaded_at        TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at         TIMESTAMP    NOT NULL DEFAULT now(),
    UNIQUE (product_variant_id)
);