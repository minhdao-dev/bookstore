CREATE TABLE review
(
    id         UUID PRIMARY KEY,
    book_id    UUID        NOT NULL REFERENCES book (id) ON DELETE CASCADE,
    user_id    UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    rating     INT         NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment    TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (book_id, user_id)
);

CREATE INDEX idx_review_book_id ON review (book_id);