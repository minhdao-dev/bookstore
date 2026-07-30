ALTER TABLE orders
    DROP CONSTRAINT orders_status_check;

ALTER TABLE orders
    ADD CONSTRAINT orders_status_check
        CHECK (status IN ('DRAFT', 'PENDING_PAYMENT', 'PAID', 'CANCELLED', 'FAILED', 'EXPIRED'));