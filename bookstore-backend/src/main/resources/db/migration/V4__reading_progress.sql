CREATE TABLE reading_progress
(
    id                 UUID PRIMARY KEY,
    user_id            UUID      NOT NULL REFERENCES users (id),
    product_variant_id UUID      NOT NULL REFERENCES product_variant (id),
    position           TEXT,
    playback_speed     NUMERIC(3, 2),
    updated_at         TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (user_id, product_variant_id)
);