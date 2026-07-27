ALTER TABLE order_line_item
    DROP CONSTRAINT order_line_item_fulfillment_status_check;

ALTER TABLE order_line_item
    ADD CONSTRAINT order_line_item_fulfillment_status_check
        CHECK (fulfillment_status IN
               ('PENDING', 'PACKING', 'SHIPPED', 'DELIVERED', 'FULFILLED', 'RETURNED', 'CANCELLED'));