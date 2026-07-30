CREATE INDEX idx_product_variant_book_id ON product_variant (book_id);

CREATE INDEX idx_orders_user_id ON orders (user_id);

CREATE INDEX idx_orders_status_updated_at ON orders (status, updated_at);

CREATE INDEX idx_order_line_item_order_id_variant_id
    ON order_line_item (order_id, product_variant_id);

CREATE INDEX idx_entitlement_user_variant_status
    ON entitlement (user_id, product_variant_id, status);

CREATE INDEX idx_entitlement_order_line_item_id ON entitlement (order_line_item_id);

CREATE INDEX idx_shipment_order_id ON shipment (order_id);

CREATE INDEX idx_shipment_tracking_number ON shipment (tracking_number);