ALTER TABLE orders
    ADD COLUMN ship_recipient_name VARCHAR(255),
    ADD COLUMN ship_phone          VARCHAR(20),
    ADD COLUMN ship_address_line   VARCHAR(500),
    ADD COLUMN ship_province_name  VARCHAR(100),
    ADD COLUMN ship_district_id    INT,
    ADD COLUMN ship_ward_code      VARCHAR(20),
    ADD COLUMN shipping_fee        NUMERIC(10, 2);

ALTER TABLE warehouse
    ADD COLUMN ghn_district_id INT,
    ADD COLUMN ghn_ward_code   VARCHAR(20);

ALTER TABLE shipment
    ADD COLUMN recipient_name VARCHAR(255),
    ADD COLUMN phone          VARCHAR(20),
    ADD COLUMN district_id    INT,
    ADD COLUMN ward_code      VARCHAR(20);