ALTER TABLE book
    ADD COLUMN search_vector tsvector;

CREATE INDEX idx_book_search_vector ON book USING GIN (search_vector);

CREATE FUNCTION book_search_vector_update() RETURNS trigger AS $$
BEGIN NEW.search_vector :=
        setweight(to_tsvector('simple', coalesce(NEW.title, '')), 'A') ||
        setweight(to_tsvector('simple', coalesce(NEW.author, '')), 'B') ||
        setweight(to_tsvector('simple', coalesce(NEW.genre, '')), 'B') ||
        setweight(to_tsvector('simple', coalesce(NEW.description, '')), 'C');
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER book_search_vector_trigger
    BEFORE INSERT OR UPDATE ON book
    FOR EACH ROW
EXECUTE FUNCTION book_search_vector_update();