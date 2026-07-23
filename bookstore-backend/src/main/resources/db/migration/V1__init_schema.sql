CREATE TABLE users
(
    id            UUID PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('CUSTOMER', 'ADMIN')),
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE book
(
    id             UUID PRIMARY KEY,
    title          VARCHAR(500) NOT NULL,
    author         VARCHAR(255) NOT NULL,
    genre          VARCHAR(100),
    language       VARCHAR(50),
    description    TEXT,
    published_date DATE,
    created_at     TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE product_variant
(
    id             UUID PRIMARY KEY,
    book_id        UUID           NOT NULL REFERENCES book (id),
    product_type   VARCHAR(20)    NOT NULL CHECK (product_type IN ('DIGITAL', 'PHYSICAL')),
    variant_format VARCHAR(20)    NOT NULL CHECK (variant_format IN ('EBOOK', 'AUDIOBOOK', 'PAPERBACK', 'HARDCOVER')),
    sku            VARCHAR(100)   NOT NULL UNIQUE,
    price          NUMERIC(10, 2) NOT NULL,
    currency       VARCHAR(3)     NOT NULL,
    weight         NUMERIC(10, 3),
    dimensions     VARCHAR(100),
    status         VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_physical_only_attrs CHECK (
        product_type = 'PHYSICAL' OR (weight IS NULL AND dimensions IS NULL)
        )
);

CREATE TABLE warehouse
(
    id      UUID PRIMARY KEY,
    name    VARCHAR(255) NOT NULL,
    address VARCHAR(500)
);

CREATE TABLE orders
(
    id           UUID PRIMARY KEY,
    user_id      UUID           NOT NULL REFERENCES users (id),
    status       VARCHAR(20)    NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN ('DRAFT', 'PENDING_PAYMENT', 'PAID', 'CANCELLED', 'FAILED')),
    total_amount NUMERIC(10, 2) NOT NULL,
    currency     VARCHAR(3)     NOT NULL,
    created_at   TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP      NOT NULL DEFAULT now()
);

CREATE TABLE order_line_item
(
    id                 UUID PRIMARY KEY,
    order_id           UUID           NOT NULL REFERENCES orders (id),
    product_variant_id UUID           NOT NULL REFERENCES product_variant (id),
    quantity           INT            NOT NULL DEFAULT 1,
    unit_price         NUMERIC(10, 2) NOT NULL,
    ownership_type     VARCHAR(20)    NOT NULL DEFAULT 'PURCHASE'
        CHECK (ownership_type IN ('PURCHASE', 'RENTAL')),
    fulfillment_status VARCHAR(20)    NOT NULL DEFAULT 'PENDING'
        CHECK (fulfillment_status IN ('PENDING', 'FULFILLED'))
);

CREATE TABLE entitlement
(
    id                 UUID PRIMARY KEY,
    user_id            UUID        NOT NULL REFERENCES users (id),
    product_variant_id UUID        NOT NULL REFERENCES product_variant (id),
    order_line_item_id UUID REFERENCES order_line_item (id),
    ownership_type     VARCHAR(20) NOT NULL CHECK (ownership_type IN ('PURCHASE', 'RENTAL', 'SUBSCRIPTION')),
    granted_at         TIMESTAMP   NOT NULL DEFAULT now(),
    expires_at         TIMESTAMP,
    status             VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'EXPIRED', 'REVOKED'))
);

CREATE TABLE inventory
(
    id                 UUID PRIMARY KEY,
    product_variant_id UUID NOT NULL REFERENCES product_variant (id),
    warehouse_id       UUID NOT NULL REFERENCES warehouse (id),
    quantity_on_hand   INT  NOT NULL DEFAULT 0,
    quantity_reserved  INT  NOT NULL DEFAULT 0,
    UNIQUE (product_variant_id, warehouse_id)
);

CREATE TABLE shipment
(
    id              UUID PRIMARY KEY,
    order_id        UUID        NOT NULL REFERENCES orders (id),
    carrier         VARCHAR(50),
    tracking_number VARCHAR(100),
    status          VARCHAR(20) NOT NULL DEFAULT 'PACKING'
        CHECK (status IN ('PACKING', 'SHIPPED', 'IN_TRANSIT', 'DELIVERED', 'RETURNED', 'FAILED')),
    shipping_fee    NUMERIC(10, 2),
    address_line    VARCHAR(500),
    city            VARCHAR(100)
);

CREATE TABLE payment_transaction
(
    id                     UUID PRIMARY KEY,
    order_id               UUID           NOT NULL REFERENCES orders (id),
    gateway                VARCHAR(20)    NOT NULL CHECK (gateway IN ('VNPAY', 'MOMO')),
    gateway_transaction_id VARCHAR(100),
    amount                 NUMERIC(15, 2) NOT NULL,
    currency               VARCHAR(3)     NOT NULL,
    status                 VARCHAR(20)    NOT NULL DEFAULT 'INITIATED'
        CHECK (status IN ('INITIATED', 'SUCCESS', 'FAILED')),
    created_at             TIMESTAMP      NOT NULL DEFAULT now()
);

CREATE INDEX idx_payment_transaction_order_id ON payment_transaction (order_id);