ALTER TABLE order_line_item
    ADD COLUMN warehouse_id UUID REFERENCES warehouse (id);