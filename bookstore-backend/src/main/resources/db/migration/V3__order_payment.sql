ALTER TABLE orders
    ADD CONSTRAINT chk_orders_status
        CHECK (status IN ('DRAFT', 'PENDING_PAYMENT', 'PAID', 'CANCELLED', 'FAILED'));

ALTER TABLE orders
    ALTER COLUMN status SET DEFAULT 'DRAFT';

ALTER TABLE order_line_item
    ADD CONSTRAINT chk_order_line_item_fulfillment_status
        CHECK (fulfillment_status IN ('PENDING', 'FULFILLED'));

ALTER TABLE order_line_item
    ALTER COLUMN fulfillment_status SET DEFAULT 'PENDING';

CREATE TABLE payment_transaction
(
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id),
    gateway                VARCHAR(20)    NOT NULL CHECK (gateway IN ('VNPAY', 'MOMO')),
    gateway_transaction_id VARCHAR(100),
    amount                 NUMERIC(15, 2) NOT NULL,
    currency               VARCHAR(3)     NOT NULL,
    status                 VARCHAR(20)    NOT NULL DEFAULT 'INITIATED'
        CHECK (status IN ('INITIATED', 'SUCCESS', 'FAILED')),
    created_at             TIMESTAMP      NOT NULL DEFAULT now()
);

CREATE INDEX idx_payment_transaction_order_id ON payment_transaction (order_id);